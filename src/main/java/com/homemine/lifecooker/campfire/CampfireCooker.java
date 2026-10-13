package com.homemine.lifecooker.campfire;

import com.homemine.lifecooker.cook.CookingLogic;
import com.homemine.lifecooker.cook.CookingOwnerHost;
import com.homemine.lifecooker.cook.CookingRecipe;
import com.homemine.lifecooker.cook.CookingStationKind;
import com.homemine.lifecooker.cook.MachineProficiencyOwner;
import com.homemine.lifecooker.cook.PlayerOwnerTracker;
import com.homemine.lifecooker.cook.ProficiencyOwner;
import com.homemine.lifecooker.items.StationItemHandler;
import com.homemine.lifecooker.menu.CookingMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * 单个原版营火上的模组侧烹饪状态：4 格食材 + 1 格产出，以及烹饪进度。
 * <p>
 * 营火方块实体（{@code CampfireBlockEntity}）不可改造，故状态存放在 {@link CampfireCookingData}
 * 中并按方块坐标索引；本类即坐标对应的那一份状态，同时充当烹饪界面的 {@link Container} 与 {@link MenuProvider}。
 * <p>
 * 槽位数固定为 5（无燃料槽），故 {@code CookingMenu.hasFuelSlot()} 判定为 false；营火无需任何燃料。
 */
public class CampfireCooker implements Container, MenuProvider, CookingOwnerHost {
    public static final int INGREDIENT_SLOTS = CookingLogic.INGREDIENT_SLOTS;
    public static final int OUTPUT_SLOT = CookingLogic.OUTPUT_SLOT;
    /** 4 格食材 + 1 格产出，无燃料槽。 */
    public static final int SLOT_COUNT = OUTPUT_SLOT + 1;

    /** 与 {@code CookingStationBlockEntity} 保持一致的同步数据布局。 */
    private static final int DATA_COUNT = 4;
    private static final int DATA_PROGRESS = 0;
    private static final int DATA_TOTAL = 1;
    private static final int DATA_KIND = 2;
    private static final int DATA_FUEL = 3;

    private final BlockPos pos;
    private final Runnable markDirty;
    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final IItemHandler itemHandler = new StationItemHandler(this, OUTPUT_SLOT);
    private final CookingLogic logic;
    private final com.homemine.lifecooker.cook.CookingQueue queue;
    @Override
    public com.homemine.lifecooker.cook.CookingQueue getQueue() { return queue; }
    private final PlayerOwnerTracker playerOwner = new PlayerOwnerTracker();

    private int lastSignal = -1;
    public int getInputSignal() {
        int count = 0;
        for (int i = 0; i < 4; i++) if (!items.get(i).isEmpty()) count++;
        return count * 2;
    }
    private int cookingProgress;
    private int cookingTotal;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_PROGRESS -> cookingProgress;
                case DATA_TOTAL -> cookingTotal;
                case DATA_KIND -> CookingStationKind.CAMPFIRE.ordinal();
                case DATA_FUEL -> 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            // 服务端权威，客户端只读
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public CampfireCooker(BlockPos pos, Runnable markDirty) {
        this.pos = pos.immutable();
        this.markDirty = markDirty;
        this.logic = new CookingLogic(this.items, this::resolveOwner, this::canCook);
        this.queue = new com.homemine.lifecooker.cook.CookingQueue(items, logic);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public IItemHandler getItemHandler() {
        return this.itemHandler;
    }

    public ContainerData getDataAccess() {
        return this.dataAccess;
    }

    public int getCookingProgress() {
        return this.cookingProgress;
    }

    public int getCookingTotal() {
        return this.cookingTotal;
    }

    // ------------------------------------------------------------------
    // 档位限制（spec §7.2）
    // ------------------------------------------------------------------

    /** 营火：简易料理恒可；复杂料理需归属 owner 达到大师；精致料理完全不可。 */
    private boolean canCook(ProficiencyOwner owner, RecipeHolder<CookingRecipe> recipe) {
        return switch (recipe.value().tier()) {
            case SIMPLE -> true;
            case COMPLEX -> owner.proficiencyLevel(recipe.id()) >= CookingLogic.MASTER_PROFICIENCY;
            case REFINED -> false;
        };
    }

    // ------------------------------------------------------------------
    // 熟练度归属（spec §9.2）
    // ------------------------------------------------------------------

    /** 机器 owner 的归属标识：本营火坐标。 */
    private String ownerId() {
        return CookingStationKind.CAMPFIRE.getSerializedName() + "@" + this.pos.toShortString();
    }

    /** 归属解析：有玩家操作者则该玩家，否则记在本营火上（spec §9.2 ①③）。 */
    private ProficiencyOwner resolveOwner(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            ProficiencyOwner player = this.playerOwner.resolve(serverLevel);
            if (player != null) {
                return player;
            }
            return new MachineProficiencyOwner(serverLevel, this.ownerId());
        }
        return ProficiencyOwner.NONE;
    }

    @Override
    public void markPlayerOwner(ServerPlayer player) {
        this.playerOwner.mark(player);
    }

    // ------------------------------------------------------------------
    // 烹饪推进（配方查找 + 进度 + 产出，实现见 CookingLogic）
    // ------------------------------------------------------------------

    /** 服务端每 tick 调用；营火无燃料，故直接推进。 */
    public void serverTick(ServerLevel level) {
        boolean full = true;
        for (int i = 0; i < 4; i++) full &= !items.get(i).isEmpty();
        if (full && level.hasNeighborSignal(pos) && queue.enqueue(level, null)) setChanged();
        if (queue.tick(level, tier -> true, level.getBlockState(pos).getValue(net.minecraft.world.level.block.CampfireBlock.LIT))) setChanged();
        cookingTotal = queue.total(0);
        cookingProgress = cookingTotal - queue.remaining(0);
        int signal = getInputSignal();
        if (lastSignal != signal) {
            lastSignal = signal;
            level.updateNeighbourForOutputSignal(pos, Blocks.CAMPFIRE);
        }
    }

    // ------------------------------------------------------------------
    // Container
    // ------------------------------------------------------------------

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(this.items, slot, amount);
        if (!stack.isEmpty()) {
            this.setChanged();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        this.setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public boolean stillValid(Player player) {
        Level level = player.level();
        if (!level.getBlockState(this.pos).is(Blocks.CAMPFIRE)) {
            return false;
        }
        return player.distanceToSqr(
                this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot != OUTPUT_SLOT;
    }

    @Override
    public void setChanged() {
        this.markDirty.run();
    }

    @Override
    public void clearContent() {
        this.items.clear();
        this.setChanged();
    }

    // ------------------------------------------------------------------
    // MenuProvider
    // ------------------------------------------------------------------

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.lifecooker.campfire");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CookingMenu(containerId, inventory, this, this.dataAccess, CookingStationKind.CAMPFIRE);
    }

    // ------------------------------------------------------------------
    // 持久化（由 CampfireCookingData 驱动）
    // ------------------------------------------------------------------

    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        ContainerHelper.saveAllItems(tag, this.items, registries);
        tag.putInt("CookingProgress", this.cookingProgress);
        tag.putInt("CookingTotal", this.cookingTotal);
        queue.save(tag, registries);
        return tag;
    }

    public static CampfireCooker load(BlockPos pos, CompoundTag tag, HolderLookup.Provider registries, Runnable markDirty) {
        CampfireCooker cooker = new CampfireCooker(pos, markDirty);
        ContainerHelper.loadAllItems(tag, cooker.items, registries);
        cooker.cookingProgress = tag.getInt("CookingProgress");
        cooker.queue.load(tag, registries);
        cooker.cookingTotal = cooker.queue.total(0);
        cooker.cookingProgress = cooker.cookingTotal - cooker.queue.remaining(0);
        return cooker;
    }

    /** 是否处于烹饪中（有进度），用于决定是否需要写回存档。 */
    public boolean isCooking() {
        return this.cookingProgress != 0 || this.cookingTotal != 0;
    }

    /** 无用例（槽位全空）时无需写入存档。 */
    public boolean isEmptyAndIdle() {
        return this.isEmpty() && queue.size() == 0;
    }
}
