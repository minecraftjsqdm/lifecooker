package com.homemine.lifecooker.cook;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * 家用灶炉可用流体燃料的分档与热值表。
 * <p>
 * Ⅰ 档（熔岩）为 100% 基准：基础档（简易 / 复杂 / 精致）耗量 200 / 300 / 400 mB，
 * 其余档位按热值倍率折算（{@code 耗量 = 基准 × 100 ÷ 热值}）。
 */
public final class FluidFuelTiers {
    /** 最高档位编号（Ⅰ~Ⅶ）。 */
    public static final int MAX_TIER = 7;
    /** 基础档在 Ⅰ 档下的耗量（mB），下标对应 {@link CookingTier#ordinal()}。 */
    private static final int[] BASE_COST = {200, 300, 400};
    /** 各档热值百分比（Ⅰ 档 = 100），下标 0 未使用。 */
    private static final int[] HEAT = {0, 100, 125, 200, 250, 400, 500, 1000};
    /** 档位罗马数字，用于悬停提示。 */
    private static final String[] NUMERAL = {"", "I", "II", "III", "IV", "V", "VI", "VII"};
    /** 未列入本表、但带 {@code #c:ignites} / {@code #c:fuel} 标签的流体兜底到 Ⅲ 档。 */
    private static final int FALLBACK_TIER = 3;

    private static final TagKey<Fluid> C_IGNITES = commonTag("ignites");
    private static final TagKey<Fluid> C_FUEL = commonTag("fuel");

    private static final Map<ResourceLocation, Integer> TIER_BY_FLUID = buildTiers();

    private FluidFuelTiers() {}

    /** @return 该流体的档位编号（1~7）；不是可用燃料时返回 0。 */
    public static int tierOf(FluidStack stack) {
        return stack.isEmpty() ? 0 : tierOf(stack.getFluid());
    }

    /** @return 该流体的档位编号（1~7）；不是可用燃料时返回 0。 */
    public static int tierOf(Fluid fluid) {
        Integer tier = TIER_BY_FLUID.get(BuiltInRegistries.FLUID.getKey(fluid));
        if (tier != null) {
            return tier;
        }
        return fluid.is(C_IGNITES) || fluid.is(C_FUEL) ? FALLBACK_TIER : 0;
    }

    /** @return 档位热值百分比（Ⅰ 档 = 100）；档位非法返回 0。 */
    public static int heat(int tier) {
        return tier >= 1 && tier <= MAX_TIER ? HEAT[tier] : 0;
    }

    /** @return 档位的罗马数字（如 {@code I}）；档位非法返回 {@code ?}。 */
    public static String numeral(int tier) {
        return tier >= 1 && tier <= MAX_TIER ? NUMERAL[tier] : "?";
    }

    /** @return 该档位下制作一份指定档位料理所需的流体量（mB）；档位非法返回 0。 */
    public static int costFor(int tier, CookingTier cookingTier) {
        if (tier < 1 || tier > MAX_TIER) {
            return 0;
        }
        return Math.max(1, Math.round(BASE_COST[cookingTier.ordinal()] * 100.0F / HEAT[tier]));
    }

    /** @return 在给定槽容量下该档位可制作的份数（简易料理）。 */
    public static int portions(int tier, int capacity) {
        int cost = costFor(tier, CookingTier.SIMPLE);
        return cost <= 0 ? 0 : capacity / cost;
    }

    private static TagKey<Fluid> commonTag(String path) {
        return TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("c", path));
    }

    private static Map<ResourceLocation, Integer> buildTiers() {
        Map<ResourceLocation, Integer> map = new HashMap<>();
        put(map, 1, "minecraft:lava",
                "divinerpg:smoldering_tar_still", "divinerpg:smoldering_tar_flowing");
        put(map, 2, "immersiveengineering:creosote", "modern_industrialization:creosote",
                "createaddition:seed_oil", "createaddition:flowing_seed_oil",
                "immersiveengineering:plantoil", "modern_industrialization:plant_oil",
                "modern_industrialization:raw_biodiesel",
                "immersivepetroleum:crudeoil", "modern_industrialization:crude_oil",
                "immersivepetroleum:lubricant", "modern_industrialization:lubricant");
        put(map, 3, "immersiveengineering:biodiesel", "modern_industrialization:biodiesel",
                "oritech:still_biofuel", "industrialforegoing:biofuel",
                "immersiveengineering:ethanol", "modern_industrialization:ethanol",
                "createaddition:bioethanol", "createaddition:flowing_bioethanol",
                "mekanismgenerators:bioethanol", "mekanismgenerators:flowing_bioethanol");
        put(map, 4, "immersivepetroleum:diesel", "immersivepetroleum:diesel_sulfur",
                "modern_industrialization:diesel", "oritech:still_diesel",
                "immersivepetroleum:naphtha", "modern_industrialization:naphtha",
                "oritech:still_naphtha", "immersivepetroleum:benzol");
        put(map, 5, "immersivepetroleum:gasoline", "immersivepetroleum:kerosene",
                "immersivepetroleum:petroleum_gas", "immersiveengineering:high_power_biodiesel",
                "modern_industrialization:light_fuel");
        put(map, 6, "modern_industrialization:heavy_fuel", "modern_industrialization:boosted_diesel",
                "immersivepetroleum:napalm");
        put(map, 7, "oritech:still_fuel", "ad_astra:fuel", "ad_astra:cryo_fuel");
        return Map.copyOf(map);
    }

    private static void put(Map<ResourceLocation, Integer> map, int tier, String... ids) {
        for (String id : ids) {
            map.put(ResourceLocation.parse(id), tier);
        }
    }
}
