package com.homemine.lifecooker.data;

import com.homemine.lifecooker.LifeCookerMod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** 自定义 Attachment 类型注册（spec §13 的 {@code data/}）。 */
public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, LifeCookerMod.MODID);

    /** 玩家的已解锁料理与熟练度（spec §10.3）；随实体存档并跨死亡保留。 */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerCookingData>> PLAYER_COOKING =
            ATTACHMENT_TYPES.register("player_cooking",
                    () -> AttachmentType.serializable(PlayerCookingData::new).copyOnDeath().build());
}
