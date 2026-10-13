package com.homemine.lifecooker.client;

import java.util.List;

import com.homemine.lifecooker.menu.CookingMenu;

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * 烹饪配方书组件：直接复用原版 {@link RecipeBookComponent} 的分类页签、点击填入与
 * 解锁状态驱动逻辑，只覆写「预览食材」的落点。
 * <p>
 * 原版默认把产物的虚影画在 {@code slots.get(0)}（合成网格第一格），而烹饪界面的第 0 格
 * 是食材槽、产出槽在 {@link CookingMenu#OUTPUT_SLOT}，故需重排虚影。
 */
@OnlyIn(Dist.CLIENT)
public class CookingRecipeBookComponent extends RecipeBookComponent {

    @Override
    public void setupGhostRecipe(RecipeHolder<?> recipe, List<Slot> slots) {
        ItemStack result = recipe.value().getResultItem(this.minecraft.level.registryAccess());
        this.ghostRecipe.setRecipe(recipe);

        Slot output = slots.get(CookingMenu.OUTPUT_SLOT);
        this.ghostRecipe.addIngredient(Ingredient.of(result), output.x, output.y);

        // 4 格食材即 4×1 的网格，原版 placeRecipe 会按顺序把原料虚影填进 0~3 号槽。
        this.placeRecipe(this.menu.getGridWidth(), this.menu.getGridHeight(),
                this.menu.getResultSlotIndex(), recipe, recipe.value().getIngredients().iterator(), 0);
    }
}
