package com.homemine.lifecooker.cook;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * 料理档位。决定该料理能在哪些烹饪载体上制作。
 */
public enum CookingTier implements StringRepresentable {
    /** 简易料理：所有烹饪方块均可制作，含营火。 */
    SIMPLE("simple"),
    /** 复杂料理：营火默认不可，熟练度达大师后可。 */
    COMPLEX("complex"),
    /** 精致料理：营火永远不可。 */
    REFINED("refined");

    public static final Codec<CookingTier> CODEC = StringRepresentable.fromEnum(CookingTier::values);

    public static final StreamCodec<ByteBuf, CookingTier> STREAM_CODEC =
            ByteBufCodecs.idMapper(i -> values()[i], CookingTier::ordinal);

    private final String name;

    CookingTier(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public String getTranslationKey() {
        return "lifecooker.tier." + this.name;
    }

    public Component getDisplayName() {
        return Component.translatable(this.getTranslationKey());
    }
}
