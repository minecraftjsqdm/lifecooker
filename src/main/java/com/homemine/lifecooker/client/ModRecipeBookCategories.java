package com.homemine.lifecooker.client;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

/**
 * 烹饪配方书分类（{@link RecipeBookCategories}）的枚举扩展入口。
 * <p>
 * 新常量由 {@code META-INF/enumextensions.json} 在运行时注入，编译期并不存在，因此只能
 * 通过 {@link EnumProxy} 持有并在运行时取回。与 {@code RecipeBookType} 不同，
 * {@link RecipeBookCategories} 是客户端类，故整个类限定在 {@link Dist#CLIENT}。
 * <p>
 * 采用的构造器是 {@code (Ljava/util/function/Supplier;)V}，参数为页签图标供应器
 * （惰性求值，图表在渲染时才需要）。
 */
@OnlyIn(Dist.CLIENT)
public final class ModRecipeBookCategories {
    /** 「搜索」页签：聚合下面全部内容分类。 */
    public static final EnumProxy<RecipeBookCategories> SEARCH = category(Items.COMPASS);
    /** 主食 / 正餐（配方 {@code category: "meal"}）。 */
    public static final EnumProxy<RecipeBookCategories> MEALS = category(Items.COOKED_BEEF);
    /** 小吃 / 点心（配方 {@code category: "snack"}）。 */
    public static final EnumProxy<RecipeBookCategories> SNACKS = category(Items.COOKIE);
    /** 饮品（配方 {@code category: "drink"}）。 */
    public static final EnumProxy<RecipeBookCategories> DRINKS = category(Items.HONEY_BOTTLE);
    /** 其他 / 未标注分类的配方。 */
    public static final EnumProxy<RecipeBookCategories> MISC = category(Items.BOWL);

    private ModRecipeBookCategories() {
    }

    public static RecipeBookCategories search() {
        return SEARCH.getValue();
    }

    public static RecipeBookCategories meals() {
        return MEALS.getValue();
    }

    public static RecipeBookCategories snacks() {
        return SNACKS.getValue();
    }

    public static RecipeBookCategories drinks() {
        return DRINKS.getValue();
    }

    public static RecipeBookCategories misc() {
        return MISC.getValue();
    }

    /** 配方 {@code category} 键 → 页签分类；未识别（含空）一律归入「其他」。 */
    public static RecipeBookCategories byKey(String key) {
        if (key == null || key.isBlank()) {
            return misc();
        }
        return switch (key) {
            case "meal" -> meals();
            case "snack" -> snacks();
            case "drink" -> drinks();
            default -> misc();
        };
    }

    private static EnumProxy<RecipeBookCategories> category(Item icon) {
        return new EnumProxy<>(RecipeBookCategories.class,
                (Supplier<List<ItemStack>>) () -> List.of(new ItemStack(icon)));
    }
}
