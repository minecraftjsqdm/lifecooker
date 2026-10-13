package com.homemine.lifecooker.blockentity;

import com.homemine.lifecooker.cook.CookingStationKind;
import com.homemine.lifecooker.cook.CookingTier;
import com.homemine.lifecooker.cook.FluidFuelTiers;
import com.homemine.lifecooker.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class StoveBlockEntity extends CookingStationBlockEntity {
    public static final int FLUID_CAPACITY = 8000;
    private final FluidTank fluidTank = new FluidTank(FLUID_CAPACITY, this::isValidFluidFuel) {
        @Override protected void onContentsChanged() { setChanged(); }
    };
    public StoveBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.STOVE.get(), pos, state, BASE_SLOT_COUNT); }
    @Override public CookingStationKind getStationKind() { return CookingStationKind.STOVE; }
    public FluidTank getFluidTank() { return fluidTank; }
    /** 罐内流体是否为可用燃料：命中分档表，或带 {@code #c:ignites} / {@code #c:fuel} 标签。 */
    protected boolean isValidFluidFuel(FluidStack stack) { return FluidFuelTiers.tierOf(stack) > 0; }
    /** @return 罐内流体的燃料档位（1~7）；无流体或非燃料返回 0。 */
    public int getFluidTier() { return FluidFuelTiers.tierOf(fluidTank.getFluid()); }
    @Override public int getFluidId() {
        FluidStack stack = fluidTank.getFluid();
        return stack.isEmpty() ? -1 : BuiltInRegistries.FLUID.getId(stack.getFluid());
    }
    @Override protected boolean payForDish(CookingTier tier) {
        int fuelTier = getFluidTier();
        if (fuelTier <= 0) return false;
        int cost = FluidFuelTiers.costFor(fuelTier, tier);
        if (fluidTank.getFluidAmount() < cost) return false;
        fluidTank.drain(cost, IFluidHandler.FluidAction.EXECUTE);
        return true;
    }
    @Override public int getFuelAmount() { return fluidTank.getFluidAmount(); }
    @Override protected int getFuelScaled(int scale) { return getFuelAmount() * scale / FLUID_CAPACITY; }
    @Override protected void saveStationData(CompoundTag tag, HolderLookup.Provider registries) { fluidTank.writeToNBT(registries, tag); }
    @Override protected void loadStationData(CompoundTag tag, HolderLookup.Provider registries) {
        fluidTank.readFromNBT(registries, tag);
        if (fluidTank.getFluidAmount() > FLUID_CAPACITY) fluidTank.getFluid().setAmount(FLUID_CAPACITY);
    }
}
