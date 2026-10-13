package com.homemine.lifecooker.data;

import com.homemine.lifecooker.cook.Proficiency;
import com.homemine.lifecooker.cook.PlayerProficiencyOwner;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Save-wide proficiency in tenths, plus durable rewards for offline initiators. */
public class MachineCookingData extends SavedData {
    public static final String DATA_NAME = "lifecooker_machine_cooking";
    private final Map<ResourceLocation, Integer> tenths = new HashMap<>();
    private final Map<UUID, Map<ResourceLocation, Integer>> pending = new HashMap<>();

    public static MachineCookingData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(MachineCookingData::new, MachineCookingData::load), DATA_NAME);
    }

    public int proficiencyCount(String ignored, ResourceLocation recipe) { return tenths.getOrDefault(recipe, 0) / 10; }
    public int proficiencyLevel(String ignored, ResourceLocation recipe) { return Proficiency.levelFor(proficiencyCount(ignored, recipe)); }
    public void addProficiency(String ignored, ResourceLocation recipe) {
        tenths.merge(recipe, 1, (a, b) -> a == Integer.MAX_VALUE ? a : a + b);
        setDirty();
    }

    public void awardPlayer(ServerLevel level, UUID id, ResourceLocation recipe) {
        pending.computeIfAbsent(id, key -> new HashMap<>()).merge(recipe, 1, Integer::sum);
        setDirty();
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(id);
        if (player != null) deliver(player);
    }

    public void deliver(ServerPlayer player) {
        Map<ResourceLocation, Integer> rewards = pending.remove(player.getUUID());
        if (rewards == null) return;
        PlayerProficiencyOwner owner = new PlayerProficiencyOwner(player);
        rewards.forEach((recipe, count) -> {
            for (int i = 0; i < count; i++) owner.addProficiency(recipe);
            owner.unlock(recipe);
        });
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("SaveTenths", saveCounts(tenths));
        ListTag players = new ListTag();
        pending.forEach((id, rewards) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Player", id);
            entry.put("Rewards", saveCounts(rewards));
            players.add(entry);
        });
        tag.put("PendingPlayers", players);
        return tag;
    }

    private static ListTag saveCounts(Map<ResourceLocation, Integer> counts) {
        ListTag list = new ListTag();
        counts.forEach((recipe, count) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("Recipe", recipe.toString()); entry.putInt("Count", count); list.add(entry);
        });
        return list;
    }

    private static void loadCounts(ListTag list, Map<ResourceLocation, Integer> counts, int factor) {
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            ResourceLocation recipe = ResourceLocation.tryParse(entry.getString("Recipe"));
            if (recipe != null) counts.merge(recipe, (int)Math.min(Integer.MAX_VALUE, Math.max(0L, (long)entry.getInt("Count") * factor)),
                    (a, b) -> (int)Math.min(Integer.MAX_VALUE, (long)a + b));
        }
    }

    private static MachineCookingData load(CompoundTag tag, HolderLookup.Provider registries) {
        MachineCookingData data = new MachineCookingData();
        loadCounts(tag.getList("SaveTenths", Tag.TAG_COMPOUND), data.tenths, 1);
        if (!tag.contains("SaveTenths")) {
            ListTag owners = tag.getList("Owners", Tag.TAG_COMPOUND);
            for (int i = 0; i < owners.size(); i++) loadCounts(owners.getCompound(i).getList("Proficiency", Tag.TAG_COMPOUND), data.tenths, 10);
        }
        ListTag players = tag.getList("PendingPlayers", Tag.TAG_COMPOUND);
        for (int i = 0; i < players.size(); i++) {
            CompoundTag entry = players.getCompound(i);
            if (!entry.hasUUID("Player")) continue;
            Map<ResourceLocation, Integer> rewards = new HashMap<>();
            loadCounts(entry.getList("Rewards", Tag.TAG_COMPOUND), rewards, 1);
            data.pending.put(entry.getUUID("Player"), rewards);
        }
        return data;
    }
}
