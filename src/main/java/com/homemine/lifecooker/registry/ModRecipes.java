package com.homemine.lifecooker.registry;

import com.homemine.lifecooker.LifeCookerMod;
import com.homemine.lifecooker.cook.CookingRecipe;
import com.homemine.lifecooker.cook.CookingRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, LifeCookerMod.MODID);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, LifeCookerMod.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<CookingRecipe>> COOKING_TYPE =
            RECIPE_TYPES.register("cooking",
                    () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(LifeCookerMod.MODID, "cooking")));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CookingRecipe>> COOKING_SERIALIZER =
            RECIPE_SERIALIZERS.register("cooking", CookingRecipeSerializer::new);
}
