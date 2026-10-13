package com.homemine.lifecooker.cook;

import net.minecraft.resources.ResourceLocation;

/**
 * 一次烹饪的熟练度归属对象（spec §9.1）。
 * <p>
 * 分两类：**玩家**（配方解锁随玩家走）与**机器 / 网络**（只累计熟练度，不持有解锁状态，spec §9.3）。
 */
public interface ProficiencyOwner {
    /** 无归属时的空实现：恒为新手，且不记录任何数据（客户端 / 世界未就绪时的兜底）。 */
    ProficiencyOwner NONE = new ProficiencyOwner() {
        @Override
        public int proficiencyLevel(ResourceLocation recipeId) {
            return 0;
        }

        @Override
        public void addProficiency(ResourceLocation recipeId) {
        }

        @Override
        public boolean isUnlocked(ResourceLocation recipeId) {
            return false;
        }

        @Override
        public void unlock(ResourceLocation recipeId) {
        }
    };

    /** 该 owner 对指定料理的熟练度等级（0 新手 / 1 掌握 / 2 熟练 / 3 大师）。 */
    int proficiencyLevel(ResourceLocation recipeId);

    /** 成功制作一次：该料理熟练度 +1（spec §7.3，无效组合不调用）。 */
    void addProficiency(ResourceLocation recipeId);

    /** 该 owner 是否已解锁该料理。 */
    boolean isUnlocked(ResourceLocation recipeId);

    /** 解锁该料理；机器 / 网络 owner 为空实现（spec §9.3）。 */
    void unlock(ResourceLocation recipeId);
}
