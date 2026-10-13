package com.homemine.lifecooker.cook;

import net.minecraft.util.Mth;

/**
 * 熟练度等级换算与减时公式（spec §7.3）。
 * <p>
 * 熟练度按料理种类分别累计成功次数，等级由累计次数落入的阈值区间决定：
 * <pre>
 * 等级  中文   阈值  时间系数
 *  0    新手    0     1.00×
 *  1    掌握    10    0.85×
 *  2    熟练    30    0.70×
 *  3    大师    60    0.55×
 * </pre>
 */
public final class Proficiency {
    /** 满级（大师）等级值。 */
    public static final int MAX_LEVEL = 3;

    /** 各等级所需的累计成功次数。 */
    private static final int[] THRESHOLDS = {0, 10, 30, 60};

    /** 各等级的时间系数。 */
    private static final double[] TIME_FACTORS = {1.00D, 0.85D, 0.70D, 0.55D};

    private Proficiency() {
    }

    /** 累计成功次数 → 等级（0 新手 / 1 掌握 / 2 熟练 / 3 大师）。 */
    public static int levelFor(int craftedCount) {
        int level = 0;
        for (int i = 0; i < THRESHOLDS.length; i++) {
            if (craftedCount >= THRESHOLDS[i]) {
                level = i;
            }
        }
        return level;
    }

    /** 等级 → 时间系数。 */
    public static double timeFactor(int level) {
        return TIME_FACTORS[Mth.clamp(level, 0, MAX_LEVEL)];
    }

    /**
     * 实际烹饪耗时（tick）= {@code max(1s, floor(基础秒数 × 系数)) × 20}（spec §7.3）。
     *
     * @param baseSeconds 配方声明的基础秒数
     * @param level       归属 owner 对该料理的熟练度等级
     */
    public static int cookingTicks(int baseSeconds, int level) {
        int seconds = Math.max(1, (int) Math.floor(baseSeconds * timeFactor(level)));
        return seconds * 20;
    }
}
