package com.homemine.lifecooker.cook;

import com.homemine.lifecooker.data.ModAttachments;
import com.homemine.lifecooker.data.PlayerCookingData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/** 玩家归属：熟练度与解锁都记在该玩家的 Attachment 上（spec §9.2 ①）。 */
public record PlayerProficiencyOwner(ServerPlayer player) implements ProficiencyOwner {
    private PlayerCookingData data() {
        return this.player.getData(ModAttachments.PLAYER_COOKING.get());
    }

    @Override
    public int proficiencyLevel(ResourceLocation recipeId) {
        return this.data().proficiencyLevel(recipeId);
    }

    @Override
    public void addProficiency(ResourceLocation recipeId) {
        this.data().addProficiency(recipeId);
    }

    @Override
    public boolean isUnlocked(ResourceLocation recipeId) {
        return this.data().isUnlocked(recipeId);
    }

    @Override
    public void unlock(ResourceLocation recipeId) {
        if (this.data().unlock(recipeId)) {
            // 镜像进原版配方书：客户端据此显示该料理（spec §6.2 / §10.3）
            this.player.awardRecipesByKey(List.of(recipeId));
        }
    }
}
