package com.homemine.lifecooker.items;

import com.homemine.lifecooker.cook.CookingLogic;
import com.homemine.lifecooker.cook.CookingOwnerHost;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public class StationItemHandler extends InvWrapper {
    /** 食材输入槽数量：每格堆叠上限 1。 */
    private static final int INPUT_SLOTS = CookingLogic.INGREDIENT_SLOTS;
    private final int outputSlot;
    public StationItemHandler(Container container, int outputSlot) {
        super(container);
        this.outputSlot = outputSlot;
    }
    private int base() { return getInv().getContainerSize(); }
    @Override public int getSlots() { return base() + 6; }
    @Override public ItemStack getStackInSlot(int slot) {
        return slot < base() ? super.getStackInSlot(slot) : ((CookingOwnerHost)getInv()).getQueue().completed(slot - base());
    }
    @Override public int getSlotLimit(int slot) {
        if (slot < INPUT_SLOTS) return 1;
        return slot < base() ? super.getSlotLimit(slot) : 64;
    }
    @Override public void setStackInSlot(int slot, ItemStack stack) {
        if (slot < base()) super.setStackInSlot(slot, stack);
    }
    @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (slot >= base() || getInv() instanceof com.homemine.lifecooker.campfire.CampfireCooker) return stack;
        return super.insertItem(slot, stack, simulate);
    }
    @Override public boolean isItemValid(int slot, ItemStack stack) {
        return slot < base() && !(getInv() instanceof com.homemine.lifecooker.campfire.CampfireCooker) && super.isItemValid(slot, stack);
    }
    @Override public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot >= base()) {
            ItemStack result = ((CookingOwnerHost)getInv()).getQueue().extract(slot - base(), amount, simulate);
            if (!simulate && !result.isEmpty()) getInv().setChanged();
            return result;
        }
        return slot == outputSlot ? super.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
    }
}
