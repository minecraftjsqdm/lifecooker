package com.homemine.lifecooker.cook;

import com.homemine.lifecooker.data.MachineCookingData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

/**
 * 机器 / 网络归属：熟练度记在世界级 {@link MachineCookingData} 上，不持有解锁状态（spec §9.2 ②③ / §9.3）。
 *
 * @param ownerId 归属标识：方块坐标（纯硬塞）或 AE 网络标识（网络订单）
 */
public record MachineProficiencyOwner(ServerLevel level, String ownerId) implements ProficiencyOwner {
    @Override
    public int proficiencyLevel(ResourceLocation recipeId) {
        return MachineCookingData.get(this.level).proficiencyLevel(this.ownerId, recipeId);
    }

    @Override
    public void addProficiency(ResourceLocation recipeId) {
        MachineCookingData.get(this.level).addProficiency(this.ownerId, recipeId);
    }

    @Override
    public boolean isUnlocked(ResourceLocation recipeId) {
        return false;
    }

    @Override
    public void unlock(ResourceLocation recipeId) {
    }
}
