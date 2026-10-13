package com.homemine.lifecooker.cook;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.List;

/**
 * {@link CookingRecipe} 的序列化器：JSON / NBT 用 {@code codec()}，网络同步用 {@code streamCodec()}。
 */
public class CookingRecipeSerializer implements RecipeSerializer<CookingRecipe> {
    private static final MapCodec<CookingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CookingIngredient.CODEC.listOf().fieldOf("ingredients").forGetter(CookingRecipe::ingredients),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(CookingRecipe::result),
            CookingTier.CODEC.fieldOf("tier").forGetter(CookingRecipe::tier),
            Codec.INT.fieldOf("time").forGetter(CookingRecipe::time),
            Codec.STRING.optionalFieldOf("category", "").forGetter(CookingRecipe::category),
            FoodTrait.CODEC.listOf().optionalFieldOf("traits", List.of()).forGetter(CookingRecipe::traits)
    ).apply(instance, CookingRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, CookingRecipe> STREAM_CODEC = StreamCodec.composite(
            CookingIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), CookingRecipe::ingredients,
            ItemStack.STREAM_CODEC, CookingRecipe::result,
            CookingTier.STREAM_CODEC, CookingRecipe::tier,
            ByteBufCodecs.VAR_INT, CookingRecipe::time,
            ByteBufCodecs.STRING_UTF8, CookingRecipe::category,
            FoodTrait.STREAM_CODEC.apply(ByteBufCodecs.list()), CookingRecipe::traits,
            CookingRecipe::new
    );

    @Override
    public MapCodec<CookingRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, CookingRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
