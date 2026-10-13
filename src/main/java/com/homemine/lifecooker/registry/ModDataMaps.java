package com.homemine.lifecooker.registry;

import com.homemine.lifecooker.LifeCookerMod;
import com.homemine.lifecooker.cook.FoodTrait;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

import java.util.List;

public class ModDataMaps {
    /**
     * 食材特性（spec §8.1）：物品 → 该食材提供的特性列表。
     * <p>
     * 数据位置 {@code data/lifecooker/data_maps/item/ingredient_traits.json}。
     */
    public static final DataMapType<Item, List<FoodTrait>> INGREDIENT_TRAITS = DataMapType.builder(
                    ResourceLocation.fromNamespaceAndPath(LifeCookerMod.MODID, "ingredient_traits"),
                    Registries.ITEM,
                    FoodTrait.CODEC.listOf())
            .build();

    /** @return 该物品作为食材提供的特性；未声明时为空列表。 */
    public static List<FoodTrait> traitsOf(ItemStack stack) {
        if (stack.isEmpty()) {
            return List.of();
        }
        List<FoodTrait> traits = stack.getItemHolder().getData(INGREDIENT_TRAITS);
        return traits != null ? traits : List.of();
    }

    public static void onRegisterDataMapTypes(RegisterDataMapTypesEvent event) {
        event.register(INGREDIENT_TRAITS);
    }
}
