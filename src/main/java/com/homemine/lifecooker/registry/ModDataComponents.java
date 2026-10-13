package com.homemine.lifecooker.registry;

import com.homemine.lifecooker.LifeCookerMod;
import com.homemine.lifecooker.cook.FoodTrait;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, LifeCookerMod.MODID);

    /**
     * 物品栈携带的特性列表（spec §8.3）：
     * 料理取自配方 JSON 的 {@code traits} 声明，不可名状的食物取自食材特性的拼合弱化结果。
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<FoodTrait>>> FOOD_TRAITS =
            DATA_COMPONENTS.registerComponentType("food_traits", builder -> builder
                    .persistent(FoodTrait.CODEC.listOf())
                    .networkSynchronized(FoodTrait.STREAM_CODEC.apply(ByteBufCodecs.list())));
}
