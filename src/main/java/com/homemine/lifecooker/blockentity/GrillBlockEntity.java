package com.homemine.lifecooker.blockentity;

import com.homemine.lifecooker.cook.CookingStationKind;
import com.homemine.lifecooker.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 烧烤炉：以物品为燃料（与原版熔炉一致的 {@code burn_time}）。
 */
public class GrillBlockEntity extends CookingStationBlockEntity {
    private int litTime;
    private int litDuration;

    public GrillBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GRILL.get(), pos, state, FUEL_SLOT + 1);
    }

    @Override
    public CookingStationKind getStationKind() {
        return CookingStationKind.GRILL;
    }

    @Override
    protected boolean isValidFuel(ItemStack stack) {
        return stack.getBurnTime(null) > 0;
    }

    @Override
    protected boolean hasFuelAvailable() {
        return this.litTime > 0;
    }

    @Override
    protected boolean tickFuel() {
        boolean changed = false;
        if (this.litTime > 0) {
            this.litTime--;
            changed = true;
        }
        if (this.litTime <= 0 && this.getQueue().hasPending()) {
            ItemStack fuel = this.items.get(FUEL_SLOT);
            int burnTime = fuel.getBurnTime(null);
            if (burnTime > 0) {
                ItemStack remainder = fuel.getCraftingRemainingItem();
                fuel.shrink(1);
                if (fuel.isEmpty()) {
                    this.items.set(FUEL_SLOT, remainder);
                }
                this.litTime = burnTime;
                this.litDuration = burnTime;
                changed = true;
            }
        }
        return changed;
    }

    @Override
    protected int getFuelScaled(int scale) {
        if (this.litDuration <= 0) {
            return 0;
        }
        return Math.min(scale, this.litTime * scale / this.litDuration);
    }

    /** 烧烤炉的"余量"即剩余燃烧 tick，供界面显示剩余燃烧时间（同熔炉）。 */
    @Override
    public int getFuelAmount() {
        return this.litTime;
    }

    public int getLitTime() {
        return this.litTime;
    }

    public int getLitDuration() {
        return this.litDuration;
    }

    @Override
    protected void saveStationData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("LitTime", this.litTime);
        tag.putInt("LitDuration", this.litDuration);
    }

    @Override
    protected void loadStationData(CompoundTag tag, HolderLookup.Provider registries) {
        this.litTime = tag.getInt("LitTime");
        this.litDuration = tag.getInt("LitDuration");
    }
}
