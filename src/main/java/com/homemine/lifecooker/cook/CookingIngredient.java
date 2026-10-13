package com.homemine.lifecooker.cook;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * 配方中的一项食材需求：匹配器（具体物品或标签）+ 需要的个数。
 * <p>
 * JSON 形态与规范一致，直接内联 {@link Ingredient} 的 {@code item} / {@code tag} 字段：
 * <pre>{ "item": "minecraft:beef", "count": 1 }</pre>
 */
public record CookingIngredient(Ingredient ingredient, int count) {
    public static final Codec<CookingIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Ingredient.MAP_CODEC_NONEMPTY.forGetter(CookingIngredient::ingredient),
            Codec.intRange(1, 64).optionalFieldOf("count", 1).forGetter(CookingIngredient::count)
    ).apply(instance, CookingIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CookingIngredient> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, CookingIngredient::ingredient,
            ByteBufCodecs.VAR_INT, CookingIngredient::count,
            CookingIngredient::new
    );

    /** 具体物品（非标签）视为更「具体」的匹配器，用于冲突优先级。 */
    public int specificity() {
        return this.ingredient.isSimple() ? 1 : 0;
    }
}
