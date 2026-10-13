package com.homemine.lifecooker.cook;

import net.minecraft.util.StringRepresentable;

/**
 * 烹饪载体种类。用于界面渲染分支与燃料语义区分。
 */
public enum CookingStationKind implements StringRepresentable {
    /** 烧烤炉：固体物品燃料。 */
    GRILL("grill"),
    /** 家用灶炉：流体 + 气体燃料。 */
    STOVE("stove"),
    /** 电磁炉：FE 能量。 */
    INDUCTION("induction"),
    /** 营火（原版改造）：无需燃料。 */
    CAMPFIRE("campfire");

    private final String name;

    CookingStationKind(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public boolean usesItemFuel() {
        return this == GRILL;
    }

    public boolean usesFluidFuel() {
        return this == STOVE;
    }

    public boolean usesEnergy() {
        return this == INDUCTION;
    }
}
