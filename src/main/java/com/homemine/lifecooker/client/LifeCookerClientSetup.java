package com.homemine.lifecooker.client;

import java.util.List;

import com.homemine.lifecooker.LifeCookerMod;
import com.homemine.lifecooker.cook.CookingRecipe;
import com.homemine.lifecooker.registry.ModMenus;
import com.homemine.lifecooker.registry.ModRecipeBookTypes;
import com.homemine.lifecooker.registry.ModBlockEntities;
import com.homemine.lifecooker.blockentity.CookingStationBlockEntity;
import com.homemine.lifecooker.registry.ModRecipes;

import net.minecraft.client.RecipeBookCategories;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterRecipeBookCategoriesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * 客户端专属注册：烹饪界面绑定与配方书分类接入。
 * <p>
 * 这两个事件只在逻辑客户端触发，且都引用了 {@code net.minecraft.client.*}，
 * 故整类限定 {@link Dist#CLIENT}，避免专用服务器加载客户端类。
 */
@EventBusSubscriber(modid = LifeCookerMod.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class LifeCookerClientSetup {

    private LifeCookerClientSetup() {
    }

    @SubscribeEvent
    public static void onRegisterBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.GRILL.get(), CookingStationRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.STOVE.get(), CookingStationRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.INDUCTION_COOKER.get(), CookingStationRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.COOKING.get(), CookingScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterRecipeBookCategories(RegisterRecipeBookCategoriesEvent event) {
        List<RecipeBookCategories> tabs = List.of(
                ModRecipeBookCategories.search(),
                ModRecipeBookCategories.meals(),
                ModRecipeBookCategories.snacks(),
                ModRecipeBookCategories.drinks(),
                ModRecipeBookCategories.misc());

        // 「搜索」页签聚合其余全部内容分类（与原版 FURNACE_SEARCH 同理）。
        event.registerAggregateCategory(ModRecipeBookCategories.search(), tabs.subList(1, tabs.size()));
        event.registerBookCategories(ModRecipeBookTypes.cooking(), tabs);
        event.registerRecipeCategoryFinder(ModRecipes.COOKING_TYPE.get(), holder ->
                holder.value() instanceof CookingRecipe recipe
                        ? ModRecipeBookCategories.byKey(recipe.category())
                        : ModRecipeBookCategories.misc());
    }
}
