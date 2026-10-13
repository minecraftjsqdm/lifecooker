package com.homemine.lifecooker.cook;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * 烹饪载体上「最近一次玩家操作者」的记录（spec §9.2 ①）。
 * <p>
 * 玩家在界面中放入食材时由 {@code CookingMenu} 写入；输入槽全空后由载体清除。
 * 只存 UUID，解析时再从玩家列表取回，避免持有已离线玩家的强引用。
 */
public final class PlayerOwnerTracker {
    private UUID playerId;

    public void mark(ServerPlayer player) {
        this.playerId = player.getUUID();
    }

    public void clear() {
        this.playerId = null;
    }

    public boolean isEmpty() {
        return this.playerId == null;
    }

    /** @return 该玩家的 owner；尚未记录或玩家已离线时返回 {@code null}。 */
    public ProficiencyOwner resolve(ServerLevel level) {
        if (this.playerId == null || level.getServer() == null) {
            return null;
        }
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(this.playerId);
        return player != null ? new PlayerProficiencyOwner(player) : null;
    }
}
