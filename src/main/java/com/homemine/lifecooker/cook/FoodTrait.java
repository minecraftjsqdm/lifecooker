package com.homemine.lifecooker.cook;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * 一条料理 / 食材特性：特性种类 + 数值 + 持续时间（秒）。
 * <p>
 * 具体特性种类的语义由 {@code lifecooker:ingredient_traits} DataMap 与效果承载层解释。
 */
public record FoodTrait(ResourceLocation type, int value, int duration) {
    public static final Codec<FoodTrait> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("type").forGetter(FoodTrait::type),
            Codec.INT.optionalFieldOf("value", 1).forGetter(FoodTrait::value),
            Codec.INT.optionalFieldOf("duration", 0).forGetter(FoodTrait::duration)
    ).apply(instance, FoodTrait::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FoodTrait> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, FoodTrait::type,
            ByteBufCodecs.VAR_INT, FoodTrait::value,
            ByteBufCodecs.VAR_INT, FoodTrait::duration,
            FoodTrait::new
    );
}
