package com.homemine.lifecooker.cook;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

/** Reserved ingredients and immutable results; only the head advances. */
public final class CookingQueue {
    public static final int CAPACITY = 6;
    private final List<Job> jobs = new ArrayList<>();
    private final NonNullList<ItemStack> items;
    private final CookingLogic logic;

    public CookingQueue(NonNullList<ItemStack> items, CookingLogic logic) {
        this.items = items;
        this.logic = logic;
    }

    public boolean enqueue(ServerLevel level, ServerPlayer player) {
        if (jobs.size() >= CAPACITY || !logic.createInput().isFull()) return false;
        ProficiencyOwner owner = player == null ? new MachineProficiencyOwner(level, "save") : new PlayerProficiencyOwner(player);
        CookingRecipeInput input = logic.createInput();
        RecipeHolder<CookingRecipe> recipe = logic.findRecipe(level, input, owner);
        Job job = new Job();
        job.recipe = recipe == null ? null : recipe.id();
        job.player = player == null ? null : player.getUUID();
        job.output = recipe == null ? logic.createFailedDish(input) : recipe.value().assemble(input, level.registryAccess());
        job.tier = recipe == null ? CookingTier.SIMPLE : recipe.value().tier();
        job.total = recipe == null ? CookingLogic.INVALID_COOK_TIME_SECONDS * 20 : logic.cookingTicks(recipe, owner);
        job.remaining = job.total;
        for (int i = 0; i < 4; i++) job.snapshot.set(i, items.get(i).copy());
        if (recipe == null) {
            for (int i = 0; i < 4; i++) job.reserved.set(i, items.get(i).split(items.get(i).getCount()));
        } else {
            for (CookingIngredient required : recipe.value().ingredientsBySpecificity()) {
                int remaining = required.count();
                for (int i = 0; i < 4 && remaining > 0; i++) {
                    ItemStack stack = items.get(i);
                    if (required.ingredient().test(stack)) {
                        int used = Math.min(remaining, stack.getCount());
                        if (job.reserved.get(i).isEmpty()) job.reserved.set(i, stack.copyWithCount(used));
                        else job.reserved.get(i).grow(used);
                        stack.shrink(used);
                        remaining -= used;
                    }
                }
            }
        }
        jobs.add(job);
        return true;
    }

    public boolean tick(ServerLevel level, Predicate<CookingTier> pay, boolean heat) {
        if (!items.get(4).isEmpty() && jobs.size() < CAPACITY) {
            Job legacy = new Job();
            legacy.output = items.get(4).copy();
            legacy.tier = CookingTier.SIMPLE;
            legacy.total = 1;
            legacy.paid = true;
            jobs.add(legacy);
            items.set(4, ItemStack.EMPTY);
            return true;
        }
        if (jobs.isEmpty()) return false;
        Job job = jobs.stream().filter(j -> j.remaining > 0).findFirst().orElse(null);
        if (job == null || !heat) return false;
        if (!job.paid) {
            if (!pay.test(job.tier)) return false;
            job.paid = true;
        }
        if (job.remaining > 0) job.remaining--;
        if (job.remaining == 0) {
            job.reserved.clear();
            if (job.recipe != null) {
                if (job.player != null) {
                    com.homemine.lifecooker.data.MachineCookingData.get(level).awardPlayer(level, job.player, job.recipe);
                } else new MachineProficiencyOwner(level, "save").addProficiency(job.recipe);
            }
            // Completed dishes stay in their queue cell until collected.
        }
        return true;
    }

    public boolean cancel(int index, Player player) {
        if (index < 0 || index >= jobs.size()) return false;
        Job job = jobs.remove(index);
        if (job.remaining == 0) {
            ItemStack result = job.output.copy();
            if (!player.getInventory().add(result)) player.drop(result, false);
            return true;
        }
        for (ItemStack reserved : job.reserved) {
            ItemStack rest = reserved.copy();
            for (int i = 0; i < 4 && !rest.isEmpty(); i++) {
                ItemStack existing = items.get(i);
                if (existing.isEmpty()) { items.set(i, rest.copy()); rest.setCount(0); }
                else if (ItemStack.isSameItemSameComponents(existing, rest)) {
                    int count = Math.min(rest.getCount(), existing.getMaxStackSize() - existing.getCount());
                    existing.grow(count); rest.shrink(count);
                }
            }
            if (!rest.isEmpty() && !player.getInventory().add(rest)) player.drop(rest, false);
        }
        return true;
    }

    public void drop(ServerLevel level, net.minecraft.core.BlockPos pos) {
        for (Job job : jobs) {
            if (job.remaining == 0) net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), job.output);
            else for (ItemStack stack : job.reserved)
                net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
        }
        jobs.clear();
    }

    public boolean hasPending() { return jobs.stream().anyMatch(j -> j.remaining > 0); }
    public ItemStack completed(int i) { return i >= 0 && i < jobs.size() && jobs.get(i).remaining == 0 ? jobs.get(i).output.copy() : ItemStack.EMPTY; }
    public ItemStack extract(int i, int amount, boolean simulate) {
        ItemStack result = completed(i);
        if (result.isEmpty() || amount <= 0) return ItemStack.EMPTY;
        result.setCount(Math.min(amount, result.getCount()));
        if (!simulate) {
            Job job = jobs.get(i);
            job.output.shrink(result.getCount());
            if (job.output.isEmpty()) jobs.remove(i);
        }
        return result;
    }
    public int previewTier(ServerLevel level, ServerPlayer viewer) {
        if (!logic.createInput().isFull()) return 0;
        RecipeHolder<CookingRecipe> recipe = logic.findRecipe(level, logic.createInput(), new PlayerProficiencyOwner(viewer));
        return recipe != null && new PlayerProficiencyOwner(viewer).isUnlocked(recipe.id()) ? recipe.value().tier().ordinal() + 1 : 0;
    }
    public int size() { return jobs.size(); }
    public int remaining(int i) { return i < jobs.size() ? jobs.get(i).remaining : 0; }
    public int total(int i) { return i < jobs.size() ? jobs.get(i).total : 0; }
    public ItemStack display(int i, ServerPlayer viewer) {
        if (i >= jobs.size()) return ItemStack.EMPTY;
        Job job = jobs.get(i);
        return job.remaining == 0 || job.recipe != null && new PlayerProficiencyOwner(viewer).isUnlocked(job.recipe) ? job.output.copy() : new ItemStack(com.homemine.lifecooker.registry.ModItems.UNSPEAKABLE_FOOD.get());
    }
    public ItemStack preview(ServerLevel level, ServerPlayer viewer) {
        if (!logic.createInput().isFull()) return ItemStack.EMPTY;
        ProficiencyOwner owner = new PlayerProficiencyOwner(viewer);
        RecipeHolder<CookingRecipe> recipe = logic.findRecipe(level, logic.createInput(), owner);
        return recipe != null && owner.isUnlocked(recipe.id()) ? recipe.value().assemble(logic.createInput(), level.registryAccess()) : new ItemStack(com.homemine.lifecooker.registry.ModItems.UNSPEAKABLE_FOOD.get());
    }

    public void save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Job job : jobs) {
            CompoundTag entry = new CompoundTag();
            ContainerHelper.saveAllItems(entry, job.reserved, registries);
            CompoundTag snapshot = new CompoundTag();
            ContainerHelper.saveAllItems(snapshot, job.snapshot, registries);
            entry.put("Snapshot", snapshot);
            entry.put("Output", job.output.save(registries));
            if (job.recipe != null) entry.putString("Recipe", job.recipe.toString());
            if (job.player != null) entry.putUUID("Player", job.player);
            entry.putInt("Remaining", job.remaining);
            entry.putInt("Total", job.total);
            entry.putInt("Tier", job.tier.ordinal());
            entry.putBoolean("Paid", job.paid);
            list.add(entry);
        }
        tag.put("Queue", list);
    }

    public void load(CompoundTag tag, HolderLookup.Provider registries) {
        jobs.clear();
        ListTag list = tag.getList("Queue", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(CAPACITY, list.size()); i++) {
            CompoundTag entry = list.getCompound(i);
            Job job = new Job();
            ContainerHelper.loadAllItems(entry, job.reserved, registries);
            ContainerHelper.loadAllItems(entry.getCompound("Snapshot"), job.snapshot, registries);
            job.output = ItemStack.parseOptional(registries, entry.getCompound("Output"));
            job.recipe = ResourceLocation.tryParse(entry.getString("Recipe"));
            job.player = entry.hasUUID("Player") ? entry.getUUID("Player") : null;
            job.total = Math.max(1, entry.getInt("Total"));
            job.remaining = Math.clamp(entry.getInt("Remaining"), 0, job.total);
            job.tier = CookingTier.values()[Math.clamp(entry.getInt("Tier"), 0, CookingTier.values().length - 1)];
            job.paid = entry.getBoolean("Paid");
            jobs.add(job);
        }
    }

    private static final class Job {
        final NonNullList<ItemStack> snapshot = NonNullList.withSize(4, ItemStack.EMPTY);
        final NonNullList<ItemStack> reserved = NonNullList.withSize(4, ItemStack.EMPTY);
        ItemStack output;
        ResourceLocation recipe;
        UUID player;
        CookingTier tier;
        int total;
        int remaining;
        boolean paid;
    }
}
