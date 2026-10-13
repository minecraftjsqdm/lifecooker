package com.homemine.lifecooker.cook;

import com.homemine.lifecooker.registry.ModDataComponents;
import com.homemine.lifecooker.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * 一道料理的配方：一组 {@link CookingIngredient} 需求（无序多重集）+ 产出 + 档位 / 时间 / 特性。
 * <p>
 * 匹配语义见 spec §7.1：输入 4 格构成的**多重集**与需求无序匹配（顺序无关、数量敏感）；
 * 需求全部被满足即命中，实际消耗仅取需求所列数量（多余食材留在槽内）。
 */
public class CookingRecipe implements Recipe<CookingRecipeInput> {
    private final List<CookingIngredient> ingredients;
    private final ItemStack result;
    private final CookingTier tier;
    private final int time;
    private final String category;
    private final List<FoodTrait> traits;

    public CookingRecipe(List<CookingIngredient> ingredients, ItemStack result, CookingTier tier,
                         int time, String category, List<FoodTrait> traits) {
        this.ingredients = List.copyOf(ingredients);
        this.result = result;
        this.tier = tier;
        this.time = time;
        this.category = category;
        this.traits = List.copyOf(traits);
    }

    public List<CookingIngredient> ingredients() {
        return this.ingredients;
    }

    public ItemStack result() {
        return this.result;
    }

    public CookingTier tier() {
        return this.tier;
    }

    /** 基础烹饪秒数（受熟练度系数缩放）。 */
    public int time() {
        return this.time;
    }

    public String category() {
        return this.category;
    }

    public List<FoodTrait> traits() {
        return this.traits;
    }

    /** 需求总件数。 */
    public int requiredCount() {
        int total = 0;
        for (CookingIngredient ingredient : this.ingredients) {
            total += ingredient.count();
        }
        return total;
    }

    /** 「具体程度」：由具体物品构成的项多者更具体，用于多配方命中时择一（spec §7.1）。 */
    public int specificity() {
        int score = 0;
        for (CookingIngredient ingredient : this.ingredients) {
            score += ingredient.specificity();
        }
        return score;
    }

    @Override
    public boolean matches(CookingRecipeInput input, Level level) {
        List<ItemStack> pool = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                pool.add(stack.copy());
            }
        }
        // 具体物品优先消耗，避免先被标签项「吃掉」导致具体项无货可配。
        for (CookingIngredient required : ingredientsBySpecificity()) {
            int remaining = required.count();
            for (Iterator<ItemStack> it = pool.iterator(); it.hasNext() && remaining > 0; ) {
                ItemStack candidate = it.next();
                if (required.ingredient().test(candidate)) {
                    int used = Math.min(remaining, candidate.getCount());
                    remaining -= used;
                    candidate.shrink(used);
                    if (candidate.isEmpty()) {
                        it.remove();
                    }
                }
            }
            if (remaining > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 按「具体程度」降序排列的需求。
     * <p>
     * 匹配与消耗必须共用同一顺序：先满足具体物品项，再轮到标签项，否则标签项可能
     * 抢走本应留给具体项的食材。
     */
    public List<CookingIngredient> ingredientsBySpecificity() {
        List<CookingIngredient> ordered = new ArrayList<>(this.ingredients);
        ordered.sort(Comparator.comparingInt(CookingIngredient::specificity).reversed());
        return ordered;
    }

    @Override
    public ItemStack assemble(CookingRecipeInput input, HolderLookup.Provider registries) {
        ItemStack stack = this.result.copy();
        if (!this.traits.isEmpty()) {
            stack.set(ModDataComponents.FOOD_TRAITS.get(), this.traits);
        }
        return stack;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.result;
    }

    /**
     * 按需求件数展开的原料列表（与 {@code count} 等长）。
     * <p>
     * 原版 {@code Recipe#getIngredients} 的语义是「每个待填格位一项」，配方书据此统计
     * 可制作数量（{@code StackedContents}）并自动填格；多重集里的 {@code count} 必须
     * 展开成重复项，否则 2 份同种食材会被算作 1 份。
     */
    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        for (CookingIngredient ingredient : this.ingredients) {
            for (int i = 0; i < ingredient.count(); i++) {
                list.add(ingredient.ingredient());
            }
        }
        return list;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.COOKING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.COOKING_TYPE.get();
    }
}
