package com.homemine.lifecooker.campfire;

import com.homemine.lifecooker.cook.CookingStationKind;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 原版营火的模组侧烹饪数据：按方块坐标索引 {@link CampfireCooker}。
 * <p>
 * {@code CampfireBlockEntity} 本身不可扩展，因此食材 / 进度 / 产出放在这里；每个维度一份。
 * 失效条目（营火被拆除）在 tick 时清理，并掉落其中物品。
 */
public class CampfireCookingData extends SavedData {
    public static final String DATA_NAME = "lifecooker_campfires";

    private static final String TAG_COOKERS = "Campfires";
    private static final String TAG_POS = "Pos";
    private static final String TAG_DATA = "Data";

    private final Map<BlockPos, CampfireCooker> cookers = new HashMap<>();

    public static CampfireCookingData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(CampfireCookingData::new, CampfireCookingData::load), DATA_NAME);
    }

    /** 取指定坐标的状态，不存在则创建（能力查询 / 打开界面时均可能触发）。 */
    public CampfireCooker getOrCreate(BlockPos pos) {
        return this.cookers.computeIfAbsent(pos.immutable(), p -> new CampfireCooker(p, this::setDirty));
    }

    /** 服务端每 tick 推进所有已加载营火，并清理失效条目。 */
    public void tick(ServerLevel level) {
        if (this.cookers.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<BlockPos, CampfireCooker>> iterator = this.cookers.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, CampfireCooker> entry = iterator.next();
            BlockPos pos = entry.getKey();
            if (!level.isLoaded(pos)) {
                // 区块未加载：位置无法校验，保留条目
                continue;
            }
            if (!level.getBlockState(pos).is(Blocks.CAMPFIRE)) {
                // 营火已被拆除：掉落模组侧物品并丢弃条目
                entry.getValue().getQueue().drop(level, pos);
                Containers.dropContents(level, pos, entry.getValue());
                iterator.remove();
                this.setDirty();
                continue;
            }
            entry.getValue().serverTick(level);
        }
    }

    /** 向玩家打开该营火的烹饪界面。 */
    public void openMenu(ServerPlayer player, BlockPos pos) {
        CampfireCooker cooker = this.getOrCreate(pos);
        player.openMenu(cooker, buf -> {
            buf.writeVarInt(cooker.getContainerSize());
            buf.writeEnum(CookingStationKind.CAMPFIRE);
        });
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Map.Entry<BlockPos, CampfireCooker> entry : this.cookers.entrySet()) {
            CampfireCooker cooker = entry.getValue();
            if (cooker.isEmptyAndIdle()) {
                // 空且未在烹饪，无需占用存档体积
                continue;
            }
            CompoundTag element = new CompoundTag();
            element.put(TAG_POS, NbtUtils.writeBlockPos(entry.getKey()));
            element.put(TAG_DATA, cooker.save(registries));
            list.add(element);
        }
        tag.put(TAG_COOKERS, list);
        return tag;
    }

    private static CampfireCookingData load(CompoundTag tag, HolderLookup.Provider registries) {
        CampfireCookingData data = new CampfireCookingData();
        ListTag list = tag.getList(TAG_COOKERS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag element = list.getCompound(i);
            NbtUtils.readBlockPos(element, TAG_POS).ifPresent(pos -> data.cookers.put(pos.immutable(),
                    CampfireCooker.load(pos, element.getCompound(TAG_DATA), registries, data::setDirty)));
        }
        return data;
    }
}
