package com.homemine.lifecooker.compat.jei;

import com.homemine.lifecooker.cook.CookingIngredient;
import com.homemine.lifecooker.cook.CookingRecipe;
import com.homemine.lifecooker.registry.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * 「料理台烹饪」JEI 分类：左侧 2×2 输入格 + 箭头 + 右侧产出格。
 * <p>
 * 配方需求按件数展开到格位（多重集语义下每个输入槽只放 1 件），与游戏内
 * 4 个输入槽「填满才可开始制作」的规则一致。
 */
public class CookingRecipeCategory extends AbstractRecipeCategory<RecipeHolder<CookingRecipe>> {
    private static final int WIDTH = 94;
    private static final int HEIGHT = 42;

    /** 2×2 输入格坐标（左上→右上→左下→右下）。 */
    private static final int[] INPUT_X = {2, 20, 2, 20};
    private static final int[] INPUT_Y = {2, 2, 20, 20};
    private static final int ARROW_X = 46;
    private static final int ARROW_Y = 13;
    private static final int OUTPUT_X = 74;
    private static final int OUTPUT_Y = 12;

    private final IDrawable arrow;

    public CookingRecipeCategory(IGuiHelper guiHelper) {
        super(LifeCookerJeiPlugin.COOKING_TYPE,
                Component.translatable("jei.lifecooker.cooking"),
                guiHelper.createDrawableItemStack(new ItemStack(ModItems.GRILL.get())),
                WIDTH, HEIGHT);
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<CookingRecipe> holder, IFocusGroup focuses) {
        CookingRecipe recipe = holder.value();
        int index = 0;
        for (CookingIngredient required : recipe.ingredientsBySpecificity()) {
            for (int i = 0; i < required.count() && index < INPUT_X.length; i++) {
                builder.addInputSlot(INPUT_X[index], INPUT_Y[index]).addIngredients(required.ingredient());
                index++;
            }
        }
        builder.addOutputSlot(OUTPUT_X, OUTPUT_Y).addItemStack(recipe.result());
    }

    @Override
    public void draw(RecipeHolder<CookingRecipe> recipe, IRecipeSlotsView slotsView,
                     GuiGraphics guiGraphics, double mouseX, double mouseY) {
        this.arrow.draw(guiGraphics, ARROW_X, ARROW_Y);
    }
}
