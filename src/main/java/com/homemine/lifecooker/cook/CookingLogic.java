package com.homemine.lifecooker.cook;

import com.homemine.lifecooker.registry.ModDataComponents;
import com.homemine.lifecooker.registry.ModDataMaps;
import com.homemine.lifecooker.registry.ModItems;
import com.homemine.lifecooker.registry.ModRecipes;
import com.homemine.lifecooker.trait.TraitBlender;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * 烹饪推进的共通实现：配方查找 → 进度累计 → 消耗食材并产出。
 * <p>
 * 作用于一段固定的槽位列表：{@code 0~3} 为食材槽，{@link #OUTPUT_SLOT} 为产出槽。
 * 三方块（{@code CookingStationBlockEntity}）与原版营火改造（{@code CampfireCooker}）共用本实现，
 * 避免档位限制与熟练度折减在两侧产生分歧。
 */
public final class CookingLogic {
    public static final int INGREDIENT_SLOTS = 4;
    public static final int OUTPUT_SLOT = 4;

    /** 无效组合（无匹配配方）时的烹饪秒数；不减时（spec §7.4）。 */
    public static final int INVALID_COOK_TIME_SECONDS = 8;

    /** 熟练度满级（大师）的等级值（spec §7.3）。 */
    public static final int MASTER_PROFICIENCY = Proficiency.MAX_LEVEL;

    private final NonNullList<ItemStack> items;
    private final Function<Level, ProficiencyOwner> ownerResolver;
    private final BiPredicate<ProficiencyOwner, RecipeHolder<CookingRecipe>> recipeFilter;

    /**
     * @param items         槽位列表，{@code 0~3} 食材、{@link #OUTPUT_SLOT} 产出；按引用读写
     * @param ownerResolver 解析本次烹饪的归属 owner（玩家 / 机器网络，spec §9.2）
     * @param recipeFilter  配方可用性过滤（档位限制等），可依据 owner 的熟练度判定
     */
    public CookingLogic(NonNullList<ItemStack> items,
                        Function<Level, ProficiencyOwner> ownerResolver,
                        BiPredicate<ProficiencyOwner, RecipeHolder<CookingRecipe>> recipeFilter) {
        this.items = items;
        this.ownerResolver = ownerResolver;
        this.recipeFilter = recipeFilter;
    }

    /** 4 格食材槽的快照。 */
    public CookingRecipeInput createInput() {
        return new CookingRecipeInput(
                this.items.get(0), this.items.get(1), this.items.get(2), this.items.get(3));
    }

    /**
     * 在食材槽中查找匹配且可烹饪的配方。
     * <p>
     * 多配方命中时取「最具体」的一条（spec §7.1）；档位限制由 {@code recipeFilter} 依据归属 owner 过滤。
     *
     * @return 命中的配方（含 ID，用于熟练度 / 解锁）；无匹配返回 {@code null}。
     */
    public RecipeHolder<CookingRecipe> findRecipe(Level level, CookingRecipeInput input) {
        if (level == null) {
            return null;
        }
        return findRecipe(level, input, this.ownerResolver.apply(level));
    }

    public RecipeHolder<CookingRecipe> findRecipe(Level level, CookingRecipeInput input, ProficiencyOwner owner) {
        return level.getRecipeManager()
                .getRecipesFor(ModRecipes.COOKING_TYPE.get(), input, level)
                .stream()
                .filter(holder -> this.recipeFilter.test(owner, holder))
                .max(Comparator.comparingInt(holder -> holder.value().specificity()))
                .orElse(null);
    }

    /** 配方耗时（tick）= 基础秒数按归属 owner 的熟练度折减后取整，至少 1 秒（spec §7.3）。 */
    public int cookingTicks(RecipeHolder<CookingRecipe> recipe, ProficiencyOwner owner) {
        return Proficiency.cookingTicks(recipe.value().time(), owner.proficiencyLevel(recipe.id()));
    }

    /**
     * 构造无效组合的产出（{@code lifecooker:unspeakable_food}）。
     * <p>
     * 特性取投入各食材的特性并按 spec §8.3 拼合弱化后写入 DataComponent。
     */
    public ItemStack createFailedDish(CookingRecipeInput input) {
        ItemStack stack = new ItemStack(ModItems.UNSPEAKABLE_FOOD.get());
        List<FoodTrait> traits = TraitBlender.blend(this.collectIngredientTraits(input));
        if (!traits.isEmpty()) {
            stack.set(ModDataComponents.FOOD_TRAITS.get(), traits);
        }
        return stack;
    }

    /** 收集投入食材的全部特性；同种食材按件数重复计入（spec §8.3 步骤 1）。 */
    private List<FoodTrait> collectIngredientTraits(CookingRecipeInput input) {
        List<FoodTrait> traits = new ArrayList<>();
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            List<FoodTrait> declared = ModDataMaps.traitsOf(stack);
            for (int i = 0; i < stack.getCount(); i++) {
                traits.addAll(declared);
            }
        }
        return traits;
    }

    /** 产出槽能否容纳该产出（同物同组件时才可叠加）。 */
    public boolean canAcceptOutput(ItemStack output) {
        if (output.isEmpty()) {
            return false;
        }
        ItemStack existing = this.items.get(OUTPUT_SLOT);
        if (existing.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameComponents(existing, output)
                && existing.getCount() + output.getCount() <= existing.getMaxStackSize();
    }

    /** @return 当前烹饪耗时（tick），四格未满、无可烹饪内容或产出槽已满时返回 -1。 */
    public int getCurrentCookingTime(Level level) {
        if (level == null) {
            return -1;
        }
        CookingRecipeInput input = this.createInput();
        if (!input.isFull()) {
            return -1;
        }
        ProficiencyOwner owner = this.ownerResolver.apply(level);
        RecipeHolder<CookingRecipe> recipe = this.findRecipe(level, input);
        ItemStack output = recipe != null
                ? recipe.value().assemble(input, level.registryAccess())
                : this.createFailedDish(input);
        if (!this.canAcceptOutput(output)) {
            return -1;
        }
        return recipe != null
                ? this.cookingTicks(recipe, owner)
                : INVALID_COOK_TIME_SECONDS * 20;
    }

    /**
     * 完成一次烹饪：命中配方则只消耗需求件数、为归属 owner 累计熟练度并解锁；
     * 否则全部消耗并产出不可名状的食物，且不计入解锁与熟练度（spec §7.4）。
     * <p>
     * 四格未满（{@link CookingRecipeInput#isFull()}）时不执行任何操作。
     */
    public void finishCooking(Level level) {
        if (level == null) {
            return;
        }
        CookingRecipeInput input = this.createInput();
        if (!input.isFull()) {
            return;
        }
        ProficiencyOwner owner = this.ownerResolver.apply(level);
        RecipeHolder<CookingRecipe> recipe = this.findRecipe(level, input);
        ItemStack output;
        if (recipe != null) {
            output = recipe.value().assemble(input, level.registryAccess());
            this.consumeIngredients(recipe.value());
            owner.addProficiency(recipe.id());
            owner.unlock(recipe.id());
        } else {
            output = this.createFailedDish(input);
            this.clearIngredients();
        }
        this.storeOutput(output);
    }

    /** 仅消耗配方所列数量，多余食材留在槽内（spec §7.1）。 */
    private void consumeIngredients(CookingRecipe recipe) {
        for (CookingIngredient required : recipe.ingredientsBySpecificity()) {
            int remaining = required.count();
            for (int slot = 0; slot < INGREDIENT_SLOTS && remaining > 0; slot++) {
                ItemStack stack = this.items.get(slot);
                if (!stack.isEmpty() && required.ingredient().test(stack)) {
                    int used = Math.min(remaining, stack.getCount());
                    stack.shrink(used);
                    remaining -= used;
                }
            }
        }
    }

    private void clearIngredients() {
        for (int slot = 0; slot < INGREDIENT_SLOTS; slot++) {
            this.items.set(slot, ItemStack.EMPTY);
        }
    }

    private void storeOutput(ItemStack output) {
        ItemStack existing = this.items.get(OUTPUT_SLOT);
        if (existing.isEmpty()) {
            this.items.set(OUTPUT_SLOT, output);
        } else {
            existing.grow(output.getCount());
        }
    }
}
