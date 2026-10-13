package com.homemine.lifecooker.client;

import com.homemine.lifecooker.blockentity.StoveBlockEntity;
import com.homemine.lifecooker.cook.CookingStationKind;
import com.homemine.lifecooker.cook.CookingTier;
import com.homemine.lifecooker.cook.FluidFuelTiers;
import com.homemine.lifecooker.menu.CookingMenu;
import com.homemine.lifecooker.registry.ModItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.textures.FluidSpriteCache;

public class CookingScreen extends AbstractContainerScreen<CookingMenu> implements RecipeUpdateListener {
    /** 剩余燃烧时间图标：直接复用原版熔炉的火焰精灵，显示方式与熔炉一致。 */
    private static final ResourceLocation LIT_PROGRESS_SPRITE =
            ResourceLocation.withDefaultNamespace("container/furnace/lit_progress");
    public final CookingRecipeBookComponent recipeBookComponent = new CookingRecipeBookComponent();
    private boolean widthTooNarrow;
    public CookingScreen(CookingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 170;
        inventoryLabelY = 80;
    }
    @Override public void init() {
        super.init();
        widthTooNarrow = width < 379;
        recipeBookComponent.init(width, height, minecraft, widthTooNarrow, menu);
        leftPos = recipeBookComponent.updateScreenPosition(width, imageWidth);
        addRenderableWidget(new ImageButton(leftPos + 8, topPos + 20, 20, 18,
                RecipeBookComponent.RECIPE_BUTTON_SPRITES, button -> {
            recipeBookComponent.toggleVisibility();
            leftPos = recipeBookComponent.updateScreenPosition(width, imageWidth);
            button.setPosition(leftPos + 8, topPos + 20);
        }));
        titleLabelX = (imageWidth - font.width(title)) / 2;
    }
    @Override public void containerTick() { super.containerTick(); recipeBookComponent.tick(); }
    private void box(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xffeeeeee);
        g.fill(x - 1, y - 1, x + w, y + h, 0xff373737);
        g.fill(x, y, x + w, y + h, 0xff8b8b8b);
    }
    @Override protected void renderBg(GuiGraphics g, float partialTick, int mx, int my) {
        int x = leftPos, y = topPos;
        g.fill(x, y, x + imageWidth, y + imageHeight, 0xff373737);
        g.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xffeeeeee);
        g.fill(x + 4, y + 4, x + imageWidth - 4, y + imageHeight - 4, 0xffc6c6c6);
        for (Slot slot : menu.slots) if (slot.x >= 0) box(g, x + slot.x, y + slot.y, 16, 16);
        CookingStationKind kind = menu.getStationKind();
        if (kind.usesItemFuel()) {
            int ix = x + CookingMenu.LIT_ICON_POS[0];
            int iy = y + CookingMenu.LIT_ICON_POS[1];
            int size = CookingMenu.LIT_ICON_SIZE;
            box(g, ix, iy, size, size);
            if (menu.fuelAmount() > 0) {
                int lit = Mth.ceil(menu.getFuelRatio() * (size - 1)) + 1;
                g.blitSprite(LIT_PROGRESS_SPRITE, size, size, 0, size - lit, ix, iy + size - lit, size, lit);
            }
        } else if (kind != CookingStationKind.CAMPFIRE) {
            int gx = x + CookingMenu.GAUGE_POS[0];
            int gy = y + CookingMenu.GAUGE_POS[1];
            int gw = CookingMenu.GAUGE_WIDTH;
            int gh = CookingMenu.GAUGE_HEIGHT;
            box(g, gx, gy, gw, gh);
            int filled = (int) (gh * menu.getFuelRatio());
            if (kind.usesEnergy()) {
                renderEnergyGauge(g, gx, gy, gw, gh, filled);
            } else {
                renderFluidGauge(g, gx, gy, gw, gh, filled);
            }
        }
        g.drawString(font, Component.literal("制作"), x + 110, y + 80, 0xff404040, false);
    }

    /** 电磁炉储能条：Forge 常见的红色储能条外观（暗红底纹 + 亮红填充 + 液面同款提亮线）。 */
    private void renderEnergyGauge(GuiGraphics g, int gx, int gy, int gw, int gh, int filled) {
        if (filled <= 0) {
            return;
        }
        int top = gy + gh - filled;
        g.fill(gx, top, gx + gw, gy + gh, 0xffb71c1c);
        for (int yy = top + 2; yy < gy + gh; yy += 3) {
            g.fill(gx, yy, gx + gw, Math.min(yy + 1, gy + gh), 0xff8e1010);
        }
        g.fill(gx, top, gx + gw, top + 1, 0x55ffffff);
    }

    /** 灶炉存量条：平铺当前流体的静止贴图，并在液面处画 1px 提亮线。 */
    private void renderFluidGauge(GuiGraphics g, int gx, int gy, int gw, int gh, int filled) {
        Fluid fluid = menu.fluid();
        if (fluid == null || filled <= 0) {
            return;
        }
        ResourceLocation still = IClientFluidTypeExtensions.of(fluid).getStillTexture();
        TextureAtlasSprite sprite = still == null ? null : FluidSpriteCache.getSprite(still);
        if (sprite == null) {
            return;
        }
        int top = gy + gh - filled;
        g.enableScissor(gx, top, gx + gw, gy + gh);
        for (int dy = 0; dy < gh; dy += 16) {
            for (int dx = 0; dx < gw; dx += 16) {
                g.blit(gx + dx, gy + dy, 0, 16, 16, sprite);
            }
        }
        g.disableScissor();
        g.fill(gx, top, gx + gw, top + 1, 0x55ffffff);
    }

    /** 流体显示名：优先取游戏内本地化名称，缺少本地化条目时退回注册表路径。 */
    private static String fluidName(Fluid fluid) {
        var type = fluid.getFluidType();
        String name = type.getDescription().getString();
        return name.isEmpty() || name.equals(type.getDescriptionId())
                ? BuiltInRegistries.FLUID.getKey(fluid).getPath().replace('_', ' ')
                : name;
    }
    private boolean unknown(int i) {
        return !menu.displayItem(i).isEmpty() && (i == 6 ? menu.previewTier() == 0
                : menu.remaining(i) > 0 && menu.displayItem(i).is(ModItems.UNSPEAKABLE_FOOD.get()));
    }
    @Override public void render(GuiGraphics g, int mx, int my, float partialTick) {
        if (recipeBookComponent.isVisible() && widthTooNarrow) {
            renderBackground(g, mx, my, partialTick);
            recipeBookComponent.render(g, mx, my, partialTick);
        } else {
            super.render(g, mx, my, partialTick);
            recipeBookComponent.render(g, mx, my, partialTick);
            recipeBookComponent.renderGhostRecipe(g, leftPos, topPos, true, partialTick);
            for (int i = 0; i < 7; i++) {
                int[] pos = i < 6 ? CookingMenu.QUEUE_POS[i] : CookingMenu.PREVIEW_POS;
                int x = leftPos + pos[0], y = topPos + pos[1];
                if (unknown(i)) {
                    g.fill(x, y, x + 16, y + 16, 0xff8b8b8b);
                    g.drawString(font, "?", x + 5, y + 3, 0xffffffff, true);
                }
                if (i < menu.queueSize() && menu.remaining(i) > 0) {
                    g.fill(x + 2, y + 13, x + 15, y + 15, 0xff000000);
                    int bar = Math.max(1, 13 * menu.remaining(i) / Math.max(1, menu.total(i)));
                    g.fill(x + 2, y + 13, x + 2 + bar, y + 14, 0xff70cc45);
                }
            }
            renderTooltip(g, mx, my);
        }
        recipeBookComponent.renderTooltip(g, leftPos, topPos, mx, my);
    }
    @Override protected void renderTooltip(GuiGraphics g, int mx, int my) {
        if (hoveredSlot != null) {
            int i = menu.displayIndex(hoveredSlot.index);
            if (i >= 0 && !menu.displayItem(i).isEmpty()) {
                var lines = new java.util.ArrayList<Component>();
                if (unknown(i)) lines.add(Component.literal("未知料理"));
                else lines.addAll(getTooltipFromContainerItem(menu.displayItem(i)));
                if (i == 6 && menu.previewTier() > 0) lines.add(Component.literal("档位：" + new String[]{"", "简易", "复杂", "精致"}[menu.previewTier()]));
                if (i < 6) lines.add(Component.literal(menu.remaining(i) == 0 ? "已完成 · 点击领取" : String.format(java.util.Locale.ROOT, "剩余 %.1f 秒 · 点击取消并退还原料", menu.remaining(i) / 20.0)));
                else lines.add(Component.literal("点击加入队列"));
                g.renderTooltip(font, lines, java.util.Optional.empty(), mx, my);
                return;
            }
        }
        if (menu.getStationKind().usesItemFuel()
                && mx >= leftPos + CookingMenu.LIT_ICON_POS[0]
                && mx < leftPos + CookingMenu.LIT_ICON_POS[0] + CookingMenu.LIT_ICON_SIZE
                && my >= topPos + CookingMenu.LIT_ICON_POS[1]
                && my < topPos + CookingMenu.LIT_ICON_POS[1] + CookingMenu.LIT_ICON_SIZE) {
            g.renderTooltip(font, Component.literal(String.format(java.util.Locale.ROOT,
                    "剩余燃烧 %.1f 秒", menu.fuelAmount() / 20.0)), mx, my);
            return;
        }
        if (mx >= leftPos + CookingMenu.GAUGE_POS[0]
                && mx < leftPos + CookingMenu.GAUGE_POS[0] + CookingMenu.GAUGE_WIDTH
                && my >= topPos + CookingMenu.GAUGE_POS[1]
                && my < topPos + CookingMenu.GAUGE_POS[1] + CookingMenu.GAUGE_HEIGHT
                && !menu.hasFuelSlot() && menu.getStationKind() != CookingStationKind.CAMPFIRE) {
            if (menu.getStationKind().usesEnergy()) {
                g.renderTooltip(font, Component.literal(menu.fuelAmount() + " / 16000 FE"), mx, my);
            } else {
                g.renderTooltip(font, fluidTooltip(), java.util.Optional.empty(), mx, my);
            }
            return;
        }
        super.renderTooltip(g, mx, my);
    }

    /** 灶炉存量条提示：燃料名与档位 / 热值 / 存量 / 单份消耗 / 满槽份数。 */
    private java.util.List<Component> fluidTooltip() {
        java.util.List<Component> lines = new java.util.ArrayList<>();
        Fluid fluid = menu.fluid();
        int tier = fluid == null ? 0 : FluidFuelTiers.tierOf(fluid);
        if (tier <= 0) {
            lines.add(Component.literal("燃料：空"));
        } else {
            lines.add(Component.literal("燃料：" + fluidName(fluid)
                    + "（" + BuiltInRegistries.FLUID.getKey(fluid) + "）," + FluidFuelTiers.numeral(tier) + "级燃料"));
            lines.add(Component.literal("热值：" + FluidFuelTiers.heat(tier) + "%（熔岩 = 100%）"));
        }
        lines.add(Component.literal("存量：" + menu.fuelAmount() + " / " + StoveBlockEntity.FLUID_CAPACITY + " mB"));
        if (tier > 0) {
            lines.add(Component.literal("单份消耗：简易 " + FluidFuelTiers.costFor(tier, CookingTier.SIMPLE)
                    + " · 复杂 " + FluidFuelTiers.costFor(tier, CookingTier.COMPLEX)
                    + " · 精致 " + FluidFuelTiers.costFor(tier, CookingTier.REFINED) + " mB"));
            lines.add(Component.literal("满槽可做：" + FluidFuelTiers.portions(tier, StoveBlockEntity.FLUID_CAPACITY) + " 份（简易）"));
        }
        return lines;
    }
    @Override public boolean mouseClicked(double x, double y, int button) {
        if (recipeBookComponent.mouseClicked(x, y, button)) return true;
        return widthTooNarrow && recipeBookComponent.isVisible() || super.mouseClicked(x, y, button);
    }
    @Override protected void slotClicked(Slot slot, int id, int button, ClickType type) {
        int i = menu.displayIndex(id);
        if (i >= 0) {
            if (button == 0 && type == ClickType.PICKUP && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, i);
            return;
        }
        super.slotClicked(slot, id, button, type);
        recipeBookComponent.slotClicked(slot);
    }
    @Override public boolean keyPressed(int key, int scan, int modifiers) {
        return recipeBookComponent.keyPressed(key, scan, modifiers) || super.keyPressed(key, scan, modifiers);
    }
    @Override public boolean charTyped(char ch, int modifiers) {
        return recipeBookComponent.charTyped(ch, modifiers) || super.charTyped(ch, modifiers);
    }
    @Override protected boolean hasClickedOutside(double x, double y, int left, int top, int button) {
        boolean outside = x < left || y < top || x >= left + imageWidth || y >= top + imageHeight;
        return recipeBookComponent.hasClickedOutside(x, y, leftPos, topPos, imageWidth, imageHeight, button) && outside;
    }
    @Override public void recipesUpdated() { recipeBookComponent.recipesUpdated(); }
    @Override public RecipeBookComponent getRecipeBookComponent() { return recipeBookComponent; }
}
