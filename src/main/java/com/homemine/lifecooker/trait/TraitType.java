package com.homemine.lifecooker.trait;

import com.homemine.lifecooker.LifeCookerMod;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * 首期特性种类（spec §8.2，约 12 种）。
 * <p>
 * 原版有对应 {@link MobEffect} 的特性在食用后直接施加该效果；「经验加成 / 抗寒 / 抗热」
 * 原版无对应效果，首期仅作为**标记**保存（{@link #effect()} 为 {@code null}），
 * 待落地方案确定后再接事件监听或自定义效果（spec §16-7）。
 */
public enum TraitType {
    ATTACK("attack", MobEffects.DAMAGE_BOOST),
    RESISTANCE("resistance", MobEffects.DAMAGE_RESISTANCE),
    HEALTH("health", MobEffects.HEALTH_BOOST),
    REGENERATION("regeneration", MobEffects.REGENERATION),
    SPEED("speed", MobEffects.MOVEMENT_SPEED),
    SATURATION("saturation", MobEffects.SATURATION),
    NIGHT_VISION("night_vision", MobEffects.NIGHT_VISION),
    HASTE("haste", MobEffects.DIG_SPEED),
    LUCK("luck", MobEffects.LUCK),
    EXPERIENCE("experience", null),
    COLD_RESISTANCE("cold_resistance", null),
    HEAT_RESISTANCE("heat_resistance", null);

    private static final Map<ResourceLocation, TraitType> BY_ID = new HashMap<>();

    static {
        for (TraitType type : values()) {
            BY_ID.put(type.id(), type);
        }
    }

    private final String name;
    private final Holder<MobEffect> effect;

    TraitType(String name, Holder<MobEffect> effect) {
        this.name = name;
        this.effect = effect;
    }

    /** 特性 ID，如 {@code lifecooker:attack}（JSON / DataComponent 中使用的键）。 */
    public ResourceLocation id() {
        return ResourceLocation.fromNamespaceAndPath(LifeCookerMod.MODID, this.name);
    }

    /** @return 承载该特性的原版效果；无对应效果时为 {@code null}。 */
    @Nullable
    public Holder<MobEffect> effect() {
        return this.effect;
    }

    /** @return 对应特性；未知 ID 返回 {@code null}（数据包可携带超出首期清单的特性）。 */
    @Nullable
    public static TraitType byId(ResourceLocation id) {
        return BY_ID.get(id);
    }
}
