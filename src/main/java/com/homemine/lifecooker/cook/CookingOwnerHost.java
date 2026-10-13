package com.homemine.lifecooker.cook;

import net.minecraft.server.level.ServerPlayer;

public interface CookingOwnerHost {
    CookingQueue getQueue();
    default void markPlayerOwner(ServerPlayer player) { }
}
