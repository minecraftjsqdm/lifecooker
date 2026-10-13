package com.homemine.lifecooker.registry;

import com.homemine.lifecooker.LifeCookerMod;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LifeCookerMod.MODID);

    public static final DeferredItem<BlockItem> GRILL = ITEMS.registerSimpleBlockItem("grill", ModBlocks.GRILL);
    public static final DeferredItem<BlockItem> STOVE = ITEMS.registerSimpleBlockItem("stove", ModBlocks.STOVE);
    public static final DeferredItem<BlockItem> INDUCTION_COOKER =
            ITEMS.registerSimpleBlockItem("induction_cooker", ModBlocks.INDUCTION_COOKER);

    /** 无效组合的产出：勉强能吃，但毫无营养价值（spec §7.5）。 */
    public static final DeferredItem<Item> UNSPEAKABLE_FOOD = ITEMS.registerSimpleItem(
            "unspeakable_food",
            new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(1)
                    .saturationModifier(0.1F)
                    .build()));

    /** 果酱：以 4 份水果熬成，涂抹类小吃。 */
    public static final DeferredItem<Item> JAM = ITEMS.registerSimpleItem(
            "jam",
            new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(6)
                    .saturationModifier(0.6F)
                    .build()));

    // ------------------------------------------------------------------
    // 新增食材（方案 C：允许新增食材物品，并作为农夫乐事食材的等价物）
    // ------------------------------------------------------------------

    /** 大米：谷物基底，兼容农夫乐事的稻米。 */
    public static final DeferredItem<Item> RICE = food("rice", 1, 0.1F);
    /** 椰子：热带果实，椰奶类料理的基础。 */
    public static final DeferredItem<Item> COCONUT = food("coconut", 3, 0.3F);
    /** 蓝莓：酸甜浆果。 */
    public static final DeferredItem<Item> BLUEBERRY = food("blueberry", 2, 0.2F);
    /** 穿天椒：辛辣，辣味料理的辣源。 */
    public static final DeferredItem<Item> CHILI = food("chili", 2, 0.2F);
    /** 番茄：多汁果实，兼容农夫乐事的番茄。 */
    public static final DeferredItem<Item> TOMATO = food("tomato", 3, 0.3F);
    /** 鲜虾：水产食材。 */
    public static final DeferredItem<Item> SHRIMP = food("shrimp", 2, 0.2F);
    /** 食用油：烹饪辅料，不可直接食用。 */
    public static final DeferredItem<Item> COOKING_OIL =
            ITEMS.registerSimpleItem("cooking_oil", new Item.Properties());

    // ------------------------------------------------------------------
    // 基础料理（同种食材 ×4，spec §11.2 /《明日之后》基础菜谱）
    // ------------------------------------------------------------------

    /** 烤肉：肉 ×4。 */
    public static final DeferredItem<Item> GRILLED_MEAT = food("grilled_meat", 9, 0.8F);
    /** 蒸烤蔬菜：蔬菜 ×4。 */
    public static final DeferredItem<Item> ROAST_VEGETABLES = food("roast_vegetables", 7, 0.7F);
    /** 烤面包：面粉 ×4。 */
    public static final DeferredItem<Item> TOAST_BREAD = food("toast_bread", 7, 0.8F);
    /** 烤蘑菇：蘑菇 ×4。 */
    public static final DeferredItem<Item> ROAST_MUSHROOM = food("roast_mushroom", 6, 0.6F);
    /** 烤鱼：鱼 ×4。 */
    public static final DeferredItem<Item> GRILLED_FISH = food("grilled_fish", 8, 0.75F);
    /** 热水：冰块 ×4。 */
    public static final DeferredItem<Item> HOT_WATER = food("hot_water", 2, 0.2F);
    /** 热牛奶：牛奶 ×4。 */
    public static final DeferredItem<Item> HOT_MILK = food("hot_milk", 4, 0.5F);
    /** 蜜糖：蜂蜜 ×4。 */
    public static final DeferredItem<Item> HONEY_SYRUP = food("honey_syrup", 4, 0.4F);

    // ------------------------------------------------------------------
    // 双食材组合料理
    // ------------------------------------------------------------------

    /** 肉串：肉 ×2 + 水果 ×2。 */
    public static final DeferredItem<Item> KEBAB = food("kebab", 8, 0.7F);
    /** 肉丸子：肉 ×2 + 蔬菜 ×2。 */
    public static final DeferredItem<Item> MEATBALL = food("meatball", 8, 0.7F);
    /** 肉馅千层饼：肉 ×2 + 面粉 ×2。 */
    public static final DeferredItem<Item> MEAT_PIE = food("meat_pie", 10, 0.85F);
    /** 蘑菇千层饼：蘑菇 ×2 + 面粉 ×2。 */
    public static final DeferredItem<Item> MUSHROOM_PIE = food("mushroom_pie", 9, 0.8F);
    /** 果味面包：水果 ×2 + 面粉 ×2。 */
    public static final DeferredItem<Item> FRUIT_BREAD = food("fruit_bread", 8, 0.7F);
    /** 果味冰棒：水果 ×1 + 冰块 ×3。 */
    public static final DeferredItem<Item> FRUIT_POPSICLE = food("fruit_popsicle", 5, 0.4F);
    /** 蜜汁烤肉：肉 ×3 + 蜂蜜 ×1。 */
    public static final DeferredItem<Item> HONEYED_MEAT = food("honeyed_meat", 11, 0.9F);
    /** 水果蜜饯：水果 ×3 + 蜂蜜 ×1。 */
    public static final DeferredItem<Item> CANDIED_FRUIT = food("candied_fruit", 6, 0.6F);
    /** 水果牛奶：水果 ×2 + 牛奶 ×2。 */
    public static final DeferredItem<Item> FRUIT_MILK = food("fruit_milk", 6, 0.6F);
    /** 奶油浓汤：肉 ×1 + 蔬菜 ×1 + 牛奶 ×2。 */
    public static final DeferredItem<Item> CREAM_SOUP = food("cream_soup", 10, 0.9F);

    // ------------------------------------------------------------------
    // 三 / 四食材组合料理
    // ------------------------------------------------------------------

    /** 水饺：肉 ×1 + 蘑菇 ×1 + 面粉 ×2。 */
    public static final DeferredItem<Item> DUMPLING = food("dumpling", 9, 0.8F);
    /** 三明治：肉 ×1 + 水果 ×1 + 面粉 ×2。 */
    public static final DeferredItem<Item> SANDWICH = food("sandwich", 8, 0.75F);
    /** 水果派：水果 ×2 + 面粉 ×1 + 蜂蜜 ×1。 */
    public static final DeferredItem<Item> FRUIT_PIE = food("fruit_pie", 9, 0.8F);
    /** 牛奶布丁：牛奶 ×2 + 蜂蜜 ×1 + 冰块 ×1。 */
    public static final DeferredItem<Item> MILK_PUDDING = food("milk_pudding", 8, 0.75F);
    /** 水果蛋糕：水果 ×1 + 面粉 ×1 + 蜂蜜 ×1 + 牛奶 ×1。 */
    public static final DeferredItem<Item> FRUIT_CAKE = food("fruit_cake", 11, 0.9F);
    /** 特制披萨：肉 ×1 + 蔬菜 ×1 + 蘑菇 ×1 + 面粉 ×1。 */
    public static final DeferredItem<Item> SPECIAL_PIZZA = food("special_pizza", 12, 1.0F);

    // ------------------------------------------------------------------
    // 《明日之后》新增料理 · 战斗增益
    // ------------------------------------------------------------------

    /** 蘑菇肉串：肉 ×1 + 蘑菇 ×1。 */
    public static final DeferredItem<Item> MUSHROOM_KEBAB = food("mushroom_kebab", 8, 0.7F);
    /** 麻辣肉丁：肉 ×1 + 穿天椒 ×1。 */
    public static final DeferredItem<Item> SPICY_DICED_MEAT = food("spicy_diced_meat", 9, 0.8F);
    /** 橙子蒸蛋：水果 ×1 + 鸡蛋 ×3。 */
    public static final DeferredItem<Item> ORANGE_STEAMED_EGG = food("orange_steamed_egg", 8, 0.75F);
    /** 蜜烧蘑菇：蘑菇 ×1 + 蜂蜜 ×1。 */
    public static final DeferredItem<Item> HONEYED_MUSHROOM = food("honeyed_mushroom", 7, 0.65F);
    /** 番茄炖肉：肉 ×1 + 番茄 ×1。 */
    public static final DeferredItem<Item> TOMATO_STEWED_MEAT = food("tomato_stewed_meat", 10, 0.85F);
    /** 鲜肉蛋饺：鸡蛋 ×2 + 肉 ×1 + 面粉 ×1。 */
    public static final DeferredItem<Item> MEAT_EGG_DUMPLING = food("meat_egg_dumpling", 11, 0.9F);

    // ------------------------------------------------------------------
    // 《明日之后》新增料理 · 采集提速
    // ------------------------------------------------------------------

    /** 蔬果沙拉：蔬菜 ×1 + 水果 ×1。 */
    public static final DeferredItem<Item> VEGGIE_FRUIT_SALAD = food("veggie_fruit_salad", 6, 0.6F);
    /** 蔬菜千层饼：蔬菜 ×1 + 面粉 ×1。 */
    public static final DeferredItem<Item> VEGETABLE_PIE = food("vegetable_pie", 8, 0.75F);
    /** 蘑菇乌冬面：蘑菇 ×1 + 大米 ×1 + 面粉 ×1。 */
    public static final DeferredItem<Item> MUSHROOM_UDON = food("mushroom_udon", 10, 0.85F);
    /** 椰奶雪花糕：椰子 ×2 + 面粉 ×1 + 牛奶 ×1。 */
    public static final DeferredItem<Item> COCONUT_MILK_CAKE = food("coconut_milk_cake", 9, 0.8F);
    /** 椰香烤肉：椰子 ×2 + 肉 ×2。 */
    public static final DeferredItem<Item> COCONUT_ROAST_MEAT = food("coconut_roast_meat", 12, 0.95F);
    /** 水果椰奶：水果 ×1 + 牛奶 ×1 + 椰子 ×1。 */
    public static final DeferredItem<Item> FRUIT_COCONUT_MILK = food("fruit_coconut_milk", 7, 0.7F);

    // ------------------------------------------------------------------
    // 《明日之后》新增料理 · 烘焙甜点
    // ------------------------------------------------------------------

    /** 蓝莓椰丝脆：面粉 ×1 + 蓝莓 ×1 + 椰子 ×1。 */
    public static final DeferredItem<Item> BLUEBERRY_COCONUT_CRISP = food("blueberry_coconut_crisp", 8, 0.75F);
    /** 葡式蛋挞：鸡蛋 ×1 + 牛奶 ×1 + 面粉 ×1 + 蜂蜜 ×1。 */
    public static final DeferredItem<Item> EGG_TART = food("egg_tart", 9, 0.8F);
    /** 奶味牛角包：面粉 ×2 + 牛奶 ×1。 */
    public static final DeferredItem<Item> MILKY_CROISSANT = food("milky_croissant", 9, 0.8F);
    /** 蜂蜜牛角包：面粉 ×2 + 蜂蜜 ×1。 */
    public static final DeferredItem<Item> HONEY_CROISSANT = food("honey_croissant", 9, 0.8F);
    /** 水果棒棒糖：水果 ×1 + 蜂蜜 ×1。 */
    public static final DeferredItem<Item> FRUIT_LOLLIPOP = food("fruit_lollipop", 5, 0.5F);

    // ------------------------------------------------------------------
    // 《明日之后》新增料理 · 蛋料理
    // ------------------------------------------------------------------

    /** 蛋羹：鸡蛋 ×2 + 牛奶 ×1。 */
    public static final DeferredItem<Item> STEAMED_EGG_CUSTARD = food("steamed_egg_custard", 8, 0.75F);
    /** 虾仁焗蛋：鲜虾 ×1 + 鸡蛋 ×2。 */
    public static final DeferredItem<Item> SHRIMP_BAKED_EGG = food("shrimp_baked_egg", 10, 0.85F);
    /** 番茄炒蛋：番茄 ×1 + 鸡蛋 ×2。 */
    public static final DeferredItem<Item> TOMATO_SCRAMBLED_EGG = food("tomato_scrambled_egg", 9, 0.8F);
    /** 凉拌西红柿：番茄 ×2 + 蜂蜜 ×1。 */
    public static final DeferredItem<Item> COLD_TOMATO = food("cold_tomato", 6, 0.6F);

    // ------------------------------------------------------------------
    // 《明日之后》新增料理 · 鱼类
    // ------------------------------------------------------------------

    /** 清炖鱼汤：鱼 ×1 + 冰块 ×1。 */
    public static final DeferredItem<Item> CLEAR_FISH_SOUP = food("clear_fish_soup", 7, 0.7F);
    /** 炸鱼饼：鱼 ×1 + 面粉 ×1 + 食用油 ×1。 */
    public static final DeferredItem<Item> FRIED_FISH_CAKE = food("fried_fish_cake", 9, 0.8F);
    /** 麻辣水煮鱼：鱼 ×1 + 穿天椒 ×2。 */
    public static final DeferredItem<Item> SPICY_BOILED_FISH = food("spicy_boiled_fish", 11, 0.9F);

    /** 注册一份普通料理：仅需营养值与饱和度，特性由配方 JSON 的 {@code traits} 注入。 */
    private static DeferredItem<Item> food(String name, int nutrition, float saturation) {
        return ITEMS.registerSimpleItem(name, new Item.Properties().food(new FoodProperties.Builder()
                .nutrition(nutrition)
                .saturationModifier(saturation)
                .build()));
    }
}
