package com.homemine.lifecooker.blockentity;

import com.homemine.lifecooker.cook.CookingLogic;
import com.homemine.lifecooker.cook.CookingOwnerHost;
import com.homemine.lifecooker.cook.CookingRecipe;
import com.homemine.lifecooker.cook.CookingRecipeInput;
import com.homemine.lifecooker.cook.CookingStationKind;
import com.homemine.lifecooker.cook.CookingTier;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * 全部烹饪载体（烧烤炉 / 家用灶炉 / 电磁炉）的公共实现。
 * <p>
 * 槽位布局：0~3 为食材输入槽，4 为产出槽，5（仅烧烤炉）为燃料槽。
 * 具体的烹饪推进在 P2 通过 {@link #getCurrentCookingTime()} / {@link #finishCooking()} 接入。
 */
public abstract class CookingStationBlockEntity extends BlockEntity implements Container, MenuProvider, CookingOwnerHost {
    public static final int INGREDIENT_SLOTS = CookingLogic.INGREDIENT_SLOTS;
    public static final int OUTPUT_SLOT = CookingLogic.OUTPUT_SLOT;
    public static final int FUEL_SLOT = 5;
    public static final int BASE_SLOT_COUNT = 5;

    public static final int DATA_COUNT = 5;
    public static final int DATA_PROGRESS = 0;
    public static final int DATA_TOTAL = 1;
    public static final int DATA_KIND = 2;
    public static final int DATA_FUEL = 3;
    /** 灶炉内流体在 {@code BuiltInRegistries.FLUID} 中的数字 id（-1 表示无流体），供客户端取贴图与名称。 */
    public static final int DATA_FLUID = 4;

    /** 燃料 / 能量 / 流体的展示比例，放大 1000 倍后同步到客户端。 */
    public static final int FUEL_SCALE = 1000;

    protected final NonNullList<ItemStack> items;
    protected int cookingProgress;
    protected int cookingTotal;

    private final IItemHandler itemHandler = new StationItemHandler(this, OUTPUT_SLOT);
    private final CookingLogic logic;
    private final com.homemine.lifecooker.cook.CookingQueue queue;
    @Override
    public com.homemine.lifecooker.cook.CookingQueue getQueue() { return queue; }
    public int getInputSignal() {
        int count = 0;
        for (int i = 0; i < 4; i++) if (!items.get(i).isEmpty()) count++;
        return count * 2;
    }
    protected boolean payForDish(CookingTier tier) { return true; }
    public int getFuelAmount() { return 0; }
    private final PlayerOwnerTracker playerOwner = new PlayerOwnerTracker();

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_PROGRESS -> cookingProgress;
                case DATA_TOTAL -> cookingTotal;
                case DATA_KIND -> getStationKind().ordinal();
                case DATA_FUEL -> getFuelScaled(FUEL_SCALE);
                case DATA_FLUID -> getFluidId();
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

    protected CookingStationBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int slotCount) {
        super(type, pos, state);
        this.items = NonNullList.withSize(slotCount, ItemStack.EMPTY);
        this.logic = new CookingLogic(this.items, this::resolveOwner, this::canCook);
        this.queue = new com.homemine.lifecooker.cook.CookingQueue(this.items, this.logic);
    }

    public abstract CookingStationKind getStationKind();

    /** 该载体能否制作指定档位的料理（营火恒不产精致，复杂需大师）。 */
    public boolean canCookTier(CookingTier tier) {
        return true;
    }

    /** 配方可用性过滤：三方块不限档位，营火覆写为档位 + 熟练度限制（spec §7.2）。 */
    protected boolean canCook(ProficiencyOwner owner, RecipeHolder<CookingRecipe> recipe) {
        return this.canCookTier(recipe.value().tier());
    }

    // ------------------------------------------------------------------
    // 熟练度归属（spec §9.2）
    // ------------------------------------------------------------------

    /** 机器 owner 的归属标识：本方块坐标（同一维度的坐标唯一）。 */
    private String ownerId() {
        return this.getStationKind().getSerializedName() + "@" + this.worldPosition.toShortString();
    }

    /** 归属解析：有玩家操作者则该玩家，否则记在本方块上（spec §9.2 ①③）。 */
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

    public float getCookingProgressRatio() {
        return this.cookingTotal <= 0 ? 0.0F : Math.min(1.0F, (float) this.cookingProgress / (float) this.cookingTotal);
    }

    public boolean isCooking() {
        return this.cookingTotal > 0 && this.cookingProgress >= 0;
    }

    // ------------------------------------------------------------------
    // 燃料相关扩展点
    // ------------------------------------------------------------------

    /** @return 当前是否具备烹饪所需燃料 / 能量。 */
    protected boolean hasFuelAvailable() {
        return true;
    }

    /** 每 tick 的燃料维护（点燃 / 消耗），返回是否需要写回数据。 */
    protected boolean tickFuel() {
        return false;
    }

    /** @return 0~scale 之间的燃料剩余比例，供界面渲染。 */
    protected int getFuelScaled(int scale) {
        return 0;
    }

    /** @return 灶炉内流体的注册表数字 id；无条件 / 无流体的载体返回 -1。 */
    public int getFluidId() {
        return -1;
    }

    // ------------------------------------------------------------------
    // 烹饪推进（配方查找 + 进度 + 产出，实现见 CookingLogic）
    // ------------------------------------------------------------------

    /** 无效组合（无匹配配方）时的烹饪秒数；不减时（spec §7.4）。 */
    public static final int INVALID_COOK_TIME_SECONDS = CookingLogic.INVALID_COOK_TIME_SECONDS;

    /** 4 格食材槽的快照。 */
    protected CookingRecipeInput createInput() {
        return this.logic.createInput();
    }

    /**
     * 在食材槽中查找匹配且可烹饪的配方。
     * <p>
     * 多配方命中时取「最具体」的一条（spec §7.1）；受档位限制（营火对复杂 / 精致料理）
     * 的配方由 {@link #canCookTier(CookingTier)} 过滤。
     *
     * @return 命中的配方；无匹配返回 {@code null}。
     */
    protected RecipeHolder<CookingRecipe> findRecipe(CookingRecipeInput input) {
        return this.logic.findRecipe(this.level, input);
    }

    /**
     * 构造无效组合的产出（{@code lifecooker:unspeakable_food}）。
     * <p>
     * 拼合弱化特性在 P3 依据 {@code input} 的各食材特性写入 DataComponent。
     */
    protected ItemStack createFailedDish(CookingRecipeInput input) {
        return this.logic.createFailedDish(input);
    }

    /** 产出槽能否容纳该产出（同物同组件时才可叠加）。 */
    protected boolean canAcceptOutput(ItemStack output) {
        return this.logic.canAcceptOutput(output);
    }

    /** @return 当前烹饪耗时（tick），无可烹饪内容或产出槽已满时返回 -1。 */
    protected int getCurrentCookingTime() {
        return this.logic.getCurrentCookingTime(this.level);
    }

    /** 完成一次烹饪：命中配方则只消耗需求件数，否则全部消耗并产出不可名状的食物。 */
    protected void finishCooking() {
        this.logic.finishCooking(this.level);
    }

    /** 该物品能否放入食材槽。 */
    protected boolean isValidIngredient(ItemStack stack) {
        return true;
    }

    /** 该物品能否作为燃料放入燃料槽（仅烧烤炉使用）。 */
    protected boolean isValidFuel(ItemStack stack) {
        return false;
    }

    /** 服务端每 tick 调用。 */
    public void serverTick() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        ServerLevel server = (ServerLevel) this.level;
        boolean changed = false;
        if (level.hasNeighborSignal(worldPosition) && getInputSignal() == 8) changed = queue.enqueue(server, null);
        changed |= this.tickFuel();
        changed |= queue.tick(server, this::payForDish, this.hasFuelAvailable());
        cookingTotal = queue.total(0);
        cookingProgress = cookingTotal - queue.remaining(0);

        if (changed) {
            this.setChanged();
            if (this.level != null) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
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
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == OUTPUT_SLOT) {
            return false;
        }
        if (slot == FUEL_SLOT) {
            return this.isValidFuel(stack);
        }
        return this.isValidIngredient(stack);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide) level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
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
        return this.getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new CookingMenu(containerId, inventory, this, this.dataAccess, this.getStationKind());
    }

    /** 由方块右键调用，向玩家打开烹饪界面。 */
    public void openMenu(Player player) {
        player.openMenu(this, buf -> {
            buf.writeVarInt(this.getContainerSize());
            buf.writeEnum(this.getStationKind());
        });
    }

    // ------------------------------------------------------------------
    // NBT
    // ------------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.items, registries);
        tag.putInt("CookingProgress", this.cookingProgress);
        tag.putInt("CookingTotal", this.cookingTotal);
        this.queue.save(tag, registries);
        this.saveStationData(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, this.items, registries);
        this.cookingProgress = tag.getInt("CookingProgress");
        this.cookingTotal = tag.getInt("CookingTotal");
        this.queue.load(tag, registries);
        this.cookingTotal = queue.total(0);
        this.cookingProgress = cookingTotal - queue.remaining(0);
        this.loadStationData(tag, registries);
    }

    protected void saveStationData(CompoundTag tag, HolderLookup.Provider registries) {
    }

    protected void loadStationData(CompoundTag tag, HolderLookup.Provider registries) {
    }
}
