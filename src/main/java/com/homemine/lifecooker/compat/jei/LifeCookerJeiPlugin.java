package com.homemine.lifecooker.compat.jei;

import com.homemine.lifecooker.LifeCookerMod;
import com.homemine.lifecooker.cook.CookingRecipe;
import com.homemine.lifecooker.registry.ModBlocks;
import com.homemine.lifecooker.registry.ModRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

/**
 * JEI 集成入口：把 {@code lifecooker:cooking} 配方以「料理台烹饪」分类展示。
 * <p>
 * JEI 为可选软依赖（{@code neoforge.mods.toml} 中声明为 optional），本类仅由 JEI 的
 * {@code @JeiPlugin} 扫描加载，未安装 JEI 时不会被触碰。
 */
@JeiPlugin
public class LifeCookerJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(LifeCookerMod.MODID, "jei_plugin");

    /** JEI 侧配方类型：与 {@link ModRecipes#COOKING_TYPE} 同名，承载带 ID 的配方实例。 */
    public static final mezz.jei.api.recipe.RecipeType<RecipeHolder<CookingRecipe>> COOKING_TYPE =
            mezz.jei.api.recipe.RecipeType.createRecipeHolderType(
                    ResourceLocation.fromNamespaceAndPath(LifeCookerMod.MODID, "cooking"));

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new CookingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        List<RecipeHolder<CookingRecipe>> recipes =
                level.getRecipeManager().getAllRecipesFor(ModRecipes.COOKING_TYPE.get());
        registration.addRecipes(COOKING_TYPE, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // 四种烹饪载体统称「料理台」：三方块 + 原版营火均可制作本模组的烹饪配方。
        registration.addRecipeCatalysts(COOKING_TYPE,
                ModBlocks.GRILL.get(),
                ModBlocks.STOVE.get(),
                ModBlocks.INDUCTION_COOKER.get(),
                Blocks.CAMPFIRE);
    }
}
