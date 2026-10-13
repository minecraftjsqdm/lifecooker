package com.homemine.lifecooker.cook;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * 烹饪配方输入：固定 4 格食材槽的快照。
 * <p>
 * 配方匹配按「无序多重集」进行，槽位顺序无关，物品数量敏感。
 */
public class CookingRecipeInput implements RecipeInput {
    public static final int SIZE = 4;

    private final ItemStack[] items;

    public CookingRecipeInput(ItemStack... items) {
        this.items = new ItemStack[SIZE];
        for (int i = 0; i < SIZE; i++) {
            this.items[i] = i < items.length ? items[i] : ItemStack.EMPTY;
        }
    }

    @Override
    public ItemStack getItem(int index) {
        return this.items[index];
    }

    @Override
    public int size() {
        return SIZE;
    }

    /** 4 个输入槽是否全部有食材：料理台只有在四格全满时才允许开始制作。 */
    public boolean isFull() {
        for (ItemStack stack : this.items) {
            if (stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
