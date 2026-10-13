package com.homemine.lifecooker.registry;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

public class ModCreativeTabs {

    public static void onBuildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModItems.GRILL.get());
            event.accept(ModItems.STOVE.get());
            event.accept(ModItems.INDUCTION_COOKER.get());
        } else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.COOKING_OIL.get());
        } else if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(ModItems.UNSPEAKABLE_FOOD.get());
            event.accept(ModItems.JAM.get());
            // 食材
            event.accept(ModItems.RICE.get());
            event.accept(ModItems.COCONUT.get());
            event.accept(ModItems.BLUEBERRY.get());
            event.accept(ModItems.CHILI.get());
            event.accept(ModItems.TOMATO.get());
            event.accept(ModItems.SHRIMP.get());
            // 基础料理
            event.accept(ModItems.GRILLED_MEAT.get());
            event.accept(ModItems.ROAST_VEGETABLES.get());
            event.accept(ModItems.TOAST_BREAD.get());
            event.accept(ModItems.ROAST_MUSHROOM.get());
            event.accept(ModItems.GRILLED_FISH.get());
            event.accept(ModItems.HOT_WATER.get());
            event.accept(ModItems.HOT_MILK.get());
            event.accept(ModItems.HONEY_SYRUP.get());
            // 双食材组合料理
            event.accept(ModItems.KEBAB.get());
            event.accept(ModItems.MEATBALL.get());
            event.accept(ModItems.MEAT_PIE.get());
            event.accept(ModItems.MUSHROOM_PIE.get());
            event.accept(ModItems.FRUIT_BREAD.get());
            event.accept(ModItems.FRUIT_POPSICLE.get());
            event.accept(ModItems.HONEYED_MEAT.get());
            event.accept(ModItems.CANDIED_FRUIT.get());
            event.accept(ModItems.FRUIT_MILK.get());
            event.accept(ModItems.CREAM_SOUP.get());
            // 三 / 四食材组合料理
            event.accept(ModItems.DUMPLING.get());
            event.accept(ModItems.SANDWICH.get());
            event.accept(ModItems.FRUIT_PIE.get());
            event.accept(ModItems.MILK_PUDDING.get());
            event.accept(ModItems.FRUIT_CAKE.get());
            event.accept(ModItems.SPECIAL_PIZZA.get());
            // 战斗增益
            event.accept(ModItems.MUSHROOM_KEBAB.get());
            event.accept(ModItems.SPICY_DICED_MEAT.get());
            event.accept(ModItems.ORANGE_STEAMED_EGG.get());
            event.accept(ModItems.HONEYED_MUSHROOM.get());
            event.accept(ModItems.TOMATO_STEWED_MEAT.get());
            event.accept(ModItems.MEAT_EGG_DUMPLING.get());
            // 采集提速
            event.accept(ModItems.VEGGIE_FRUIT_SALAD.get());
            event.accept(ModItems.VEGETABLE_PIE.get());
            event.accept(ModItems.MUSHROOM_UDON.get());
            event.accept(ModItems.COCONUT_MILK_CAKE.get());
            event.accept(ModItems.COCONUT_ROAST_MEAT.get());
            event.accept(ModItems.FRUIT_COCONUT_MILK.get());
            // 烘焙甜点
            event.accept(ModItems.BLUEBERRY_COCONUT_CRISP.get());
            event.accept(ModItems.EGG_TART.get());
            event.accept(ModItems.MILKY_CROISSANT.get());
            event.accept(ModItems.HONEY_CROISSANT.get());
            event.accept(ModItems.FRUIT_LOLLIPOP.get());
            // 蛋料理
            event.accept(ModItems.STEAMED_EGG_CUSTARD.get());
            event.accept(ModItems.SHRIMP_BAKED_EGG.get());
            event.accept(ModItems.TOMATO_SCRAMBLED_EGG.get());
            event.accept(ModItems.COLD_TOMATO.get());
            // 鱼类料理
            event.accept(ModItems.CLEAR_FISH_SOUP.get());
            event.accept(ModItems.FRIED_FISH_CAKE.get());
            event.accept(ModItems.SPICY_BOILED_FISH.get());
        }
    }
}
