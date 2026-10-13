package com.homemine.lifecooker.data;

import com.homemine.lifecooker.cook.Proficiency;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 玩家侧的烹饪数据（spec §10.3）：已解锁料理 + 各料理的熟练度累计次数。
 * <p>
 * 作为玩家 Attachment 持久化（随实体存档、跨死亡保留）。熟练度按料理种类分别累计（spec §7.3），
 * 等级由 {@link Proficiency#levelFor} 换算。
 */
public class PlayerCookingData implements INBTSerializable<CompoundTag> {
    private static final String TAG_UNLOCKED = "UnlockedRecipes";
    private static final String TAG_PROFICIENCY = "Proficiency";
    private static final String TAG_ID = "Id";
    private static final String TAG_COUNT = "Count";

    private final Set<ResourceLocation> unlockedRecipes = new HashSet<>();
    private final Map<ResourceLocation, Integer> proficiency = new HashMap<>();

    public boolean isUnlocked(ResourceLocation recipeId) {
        return this.unlockedRecipes.contains(recipeId);
    }

    /** @return 本次是否新增解锁（已解锁则返回 {@code false}）。 */
    public boolean unlock(ResourceLocation recipeId) {
        return this.unlockedRecipes.add(recipeId);
    }

    public int proficiencyCount(ResourceLocation recipeId) {
        return this.proficiency.getOrDefault(recipeId, 0);
    }

    public int proficiencyLevel(ResourceLocation recipeId) {
        return Proficiency.levelFor(this.proficiencyCount(recipeId));
    }

    /** 成功制作一次：累计次数 +1。 */
    public void addProficiency(ResourceLocation recipeId) {
        this.proficiency.merge(recipeId, 1, Integer::sum);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();

        ListTag unlocked = new ListTag();
        for (ResourceLocation id : this.unlockedRecipes) {
            unlocked.add(StringTag.valueOf(id.toString()));
        }
        tag.put(TAG_UNLOCKED, unlocked);

        ListTag proficiency = new ListTag();
        for (Map.Entry<ResourceLocation, Integer> entry : this.proficiency.entrySet()) {
            CompoundTag element = new CompoundTag();
            element.putString(TAG_ID, entry.getKey().toString());
            element.putInt(TAG_COUNT, entry.getValue());
            proficiency.add(element);
        }
        tag.put(TAG_PROFICIENCY, proficiency);

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        this.unlockedRecipes.clear();
        this.proficiency.clear();

        ListTag unlocked = tag.getList(TAG_UNLOCKED, Tag.TAG_STRING);
        for (int i = 0; i < unlocked.size(); i++) {
            ResourceLocation id = ResourceLocation.tryParse(unlocked.getString(i));
            if (id != null) {
                this.unlockedRecipes.add(id);
            }
        }

        ListTag proficiency = tag.getList(TAG_PROFICIENCY, Tag.TAG_COMPOUND);
        for (int i = 0; i < proficiency.size(); i++) {
            CompoundTag element = proficiency.getCompound(i);
            ResourceLocation id = ResourceLocation.tryParse(element.getString(TAG_ID));
            if (id != null) {
                this.proficiency.put(id, element.getInt(TAG_COUNT));
            }
        }
    }
}
