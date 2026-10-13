package com.homemine.lifecooker.menu;

import com.homemine.lifecooker.blockentity.CookingStationBlockEntity;
import com.homemine.lifecooker.cook.CookingOwnerHost;
import com.homemine.lifecooker.cook.CookingRecipe;
import com.homemine.lifecooker.cook.CookingRecipeInput;
import com.homemine.lifecooker.cook.CookingStationKind;
import com.homemine.lifecooker.registry.ModMenus;
import com.homemine.lifecooker.registry.ModRecipeBookTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * 烹饪界面菜单：4 食材槽 + 1 产出槽（+ 烧烤炉的 1 燃料槽）+ 玩家物品栏。
 * <p>
 * 继承 {@link RecipeBookMenu} 以接入原版配方书：4 格食材即 4×1 的合成网格，产出槽为结果槽。
 */
public class CookingMenu extends RecipeBookMenu<CookingRecipeInput, CookingRecipe> {
    public static final int INGREDIENT_SLOTS = CookingStationBlockEntity.INGREDIENT_SLOTS;
    public static final int OUTPUT_SLOT = CookingStationBlockEntity.OUTPUT_SLOT;
    public static final int FUEL_SLOT = CookingStationBlockEntity.FUEL_SLOT;

    /** 食材槽在界面中的坐标。 */
    public static final int[][] INGREDIENT_POS = {{54, 24}, {36, 42}, {72, 42}, {54, 60}};
    public static final int[][] QUEUE_POS = {{110, 24}, {128, 24}, {146, 24}, {110, 42}, {128, 42}, {146, 42}};
    public static final int[] PREVIEW_POS = {110, 60};
    private final SimpleContainer display = new SimpleContainer(7);
    private final SimpleContainerData queueData = new SimpleContainerData(15);
    private final Player viewer;
    private int displayStart;
    /** 产出槽坐标。 */
    public static final int[] OUTPUT_POS = {150, 84};
    public static final int[] FUEL_POS = {16, 60};
    /** 竖向存量条（灶炉流体 / 电磁炉 FE）：底边与菱形输入最下一格对齐。 */
    public static final int[] GAUGE_POS = {16, 40};
    public static final int GAUGE_WIDTH = 14;
    public static final int GAUGE_HEIGHT = 36;
    /** 烧烤炉剩余燃烧时间图标（熔炉同款火焰）：与竖存量条同位。 */
    public static final int[] LIT_ICON_POS = {16, 40};
    public static final int LIT_ICON_SIZE = 14;
    /** 烹饪进度条填充区域（与 gen_gui_assets.py 中的 ARROW_* 一致）。 */
    public static final int[] ARROW_POS = {100, 25};
    public static final int ARROW_WIDTH = 26;
    public static final int ARROW_HEIGHT = 8;

    private final Container container;
    private final ContainerData data;
    private final CookingStationKind kind;
    private final int stationSlots;
    /** 承载本次烹饪的载体（若支持玩家归属感知）；客户端侧为 {@code null}。 */
    private final CookingOwnerHost ownerHost;
    protected final Level level;

    /** 服务端构造：由方块实体 / 营火数据提供容器与同步数据。 */
    public CookingMenu(int containerId, Inventory playerInventory, Container container, ContainerData data, CookingStationKind kind) {
        super(ModMenus.COOKING.get(), containerId);
        this.container = container;
        this.data = data;
        this.kind = kind;
        this.stationSlots = container.getContainerSize();
        this.ownerHost = container instanceof CookingOwnerHost host ? host : null;
        this.level = playerInventory.player.level();
        this.viewer = playerInventory.player;

        for (int i = 0; i < INGREDIENT_SLOTS; i++) {
            this.addSlot(new IngredientSlot(container, i, INGREDIENT_POS[i][0], INGREDIENT_POS[i][1]));
        }
        this.addSlot(new OutputSlot(container, OUTPUT_SLOT, -10000, -10000));
        if (this.hasFuelSlot()) {
            this.addSlot(new FuelSlot(container, FUEL_SLOT, FUEL_POS[0], FUEL_POS[1]));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 92 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 146));
        }

        this.displayStart = this.slots.size();
        for (int i = 0; i < 7; i++) {
            int[] pos = i < 6 ? QUEUE_POS[i] : PREVIEW_POS;
            this.addSlot(new Slot(display, i, pos[0], pos[1]) {
                @Override public boolean mayPlace(ItemStack stack) { return false; }
                @Override public boolean mayPickup(Player player) { return false; }
            });
        }
        this.addDataSlots(data);
        this.addDataSlots(queueData);
    }

    /** 客户端构造：仅从网络同步槽位与进度数据。 */
    public CookingMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, new SimpleContainer(buf.readVarInt()),
                new SimpleContainerData(CookingStationBlockEntity.DATA_COUNT), buf.readEnum(CookingStationKind.class));
    }

    @Override
    public void broadcastChanges() {
        if (viewer instanceof ServerPlayer player && ownerHost != null) {
            var queue = ownerHost.getQueue();
            for (int i = 0; i < 6; i++) {
                display.setItem(i, queue.display(i, player));
                queueData.set(i * 2, Math.min(32767, queue.remaining(i)));
                queueData.set(i * 2 + 1, Math.min(32767, queue.total(i)));
            }
            display.setItem(6, queue.preview(player.serverLevel(), player));
            queueData.set(12, queue.size());
            queueData.set(13, queue.previewTier(player.serverLevel(), player));
            queueData.set(14, container instanceof CookingStationBlockEntity station ? station.getFuelAmount() : 0);
        }
        super.broadcastChanges();
    }
    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer server) || !stillValid(player) || ownerHost == null) return false;
        boolean changed = id == 6 ? ownerHost.getQueue().enqueue(server.serverLevel(), server)
                : id >= 0 && id < 6 && ownerHost.getQueue().cancel(id, player);
        if (changed) { container.setChanged(); broadcastChanges(); }
        return changed;
    }
    public int displayIndex(int slot) { return slot >= displayStart && slot < displayStart + 7 ? slot - displayStart : -1; }
    public int queueSize() { return queueData.get(12); }
    public int remaining(int i) { return queueData.get(i * 2); }
    public int total(int i) { return queueData.get(i * 2 + 1); }
    public int previewTier() { return queueData.get(13); }
    public int fuelAmount() { return queueData.get(14); }
    public ItemStack displayItem(int i) { return display.getItem(i); }

    /** @return 灶炉内流体的注册表数字 id；无流体 / 非灶炉返回 -1。 */
    public int fluidId() {
        return this.data.get(CookingStationBlockEntity.DATA_FLUID);
    }

    /** @return 灶炉内流体；无流体返回 {@code null}。 */
    public Fluid fluid() {
        Fluid fluid = BuiltInRegistries.FLUID.byId(fluidId());
        return fluid == null || fluid.isSame(Fluids.EMPTY) ? null : fluid;
    }

    public CookingStationKind getStationKind() {
        return this.kind;
    }

    public boolean hasFuelSlot() {
        return this.stationSlots > FUEL_SLOT;
    }

    public float getProgressRatio() {
        int total = this.data.get(CookingStationBlockEntity.DATA_TOTAL);
        if (total <= 0) {
            return 0.0F;
        }
        return Mth.clamp((float) this.data.get(CookingStationBlockEntity.DATA_PROGRESS) / (float) total, 0.0F, 1.0F);
    }

    public float getFuelRatio() {
        return Mth.clamp((float) this.data.get(CookingStationBlockEntity.DATA_FUEL) / CookingStationBlockEntity.FUEL_SCALE, 0.0F, 1.0F);
    }

    // ------------------------------------------------------------------
    // RecipeBookMenu
    // ------------------------------------------------------------------

    @Override
    public void fillCraftSlotsStackedContents(StackedContents itemHelper) {
        for (int i = 0; i < INGREDIENT_SLOTS; i++) {
            itemHelper.accountSimpleStack(this.container.getItem(i));
        }
    }

    @Override
    public void clearCraftingContent() {
        for (int i = 0; i < INGREDIENT_SLOTS; i++) {
            this.getSlot(i).set(ItemStack.EMPTY);
        }
    }

    @Override
    public boolean recipeMatches(RecipeHolder<CookingRecipe> recipe) {
        return recipe.value().matches(this.createRecipeInput(), this.level);
    }

    /** 当前 4 格食材的快照，供配方匹配使用。 */
    public CookingRecipeInput createRecipeInput() {
        return new CookingRecipeInput(
                this.container.getItem(0), this.container.getItem(1),
                this.container.getItem(2), this.container.getItem(3));
    }

    @Override
    public int getResultSlotIndex() {
        return OUTPUT_SLOT;
    }

    @Override
    public int getGridWidth() {
        return INGREDIENT_SLOTS;
    }

    @Override
    public int getGridHeight() {
        return 1;
    }

    @Override
    public int getSize() {
        return this.stationSlots;
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return ModRecipeBookTypes.cooking();
    }

    @Override
    public boolean shouldMoveToInventory(int slotIndex) {
        return slotIndex != OUTPUT_SLOT && slotIndex != FUEL_SLOT;
    }

    // ------------------------------------------------------------------
    // 玩家操作的归属感知（spec §9.2 ①）
    // ------------------------------------------------------------------

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= displayStart) return;
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public void handlePlacement(boolean placeAll, RecipeHolder<?> recipe, ServerPlayer player) {
        // 由配方书自动填充食材同样属于玩家操作
        if (this.ownerHost != null) {
            this.ownerHost.markPlayerOwner(player);
        }
        super.handlePlacement(placeAll, recipe, player);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= displayStart) return ItemStack.EMPTY;
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        result = stack.copy();
        int invStart = this.stationSlots;
        int invEnd = this.displayStart;

        if (index == OUTPUT_SLOT) {
            if (!this.moveItemStackTo(stack, invStart, invEnd, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, result);
        } else if (index < this.stationSlots) {
            if (!this.moveItemStackTo(stack, invStart, invEnd, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            boolean moved = false;
            if (this.hasFuelSlot() && stack.getBurnTime(null) > 0) {
                moved = this.moveItemStackTo(stack, FUEL_SLOT, FUEL_SLOT + 1, false);
            }
            if (!moved && !this.moveItemStackTo(stack, 0, INGREDIENT_SLOTS, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == result.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return result;
    }

    /** 食材槽：每格堆叠上限为 1（配方按「每格 1 件」的多重集匹配）。 */
    private static class IngredientSlot extends Slot {
        IngredientSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    /** 产出槽：只能取出，不能放入。 */
    private static class OutputSlot extends Slot {
        OutputSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }

    /** 烧烤炉燃料槽：仅接受可燃烧物品。 */
    private static class FuelSlot extends Slot {
        FuelSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getBurnTime(null) > 0;
        }
    }
}
