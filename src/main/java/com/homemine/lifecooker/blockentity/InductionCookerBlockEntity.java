package com.homemine.lifecooker.blockentity;

import com.homemine.lifecooker.cook.CookingStationKind;
import com.homemine.lifecooker.cook.CookingTier;
import com.homemine.lifecooker.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;

public class InductionCookerBlockEntity extends CookingStationBlockEntity {
    public static final int ENERGY_CAPACITY = 16000;
    public static final int MAX_RECEIVE = 1000;
    private final InternalEnergy energy = new InternalEnergy();
    private class InternalEnergy extends EnergyStorage {
        InternalEnergy() { super(ENERGY_CAPACITY, MAX_RECEIVE, 0); }
        @Override public int receiveEnergy(int amount, boolean simulate) {
            int received = super.receiveEnergy(amount, simulate);
            if (!simulate && received > 0) setChanged();
            return received;
        }
        void restore(int value) { energy = Math.clamp(value, 0, ENERGY_CAPACITY); }
        boolean consume(int amount) {
            if (energy < amount) return false;
            energy -= amount; setChanged(); return true;
        }
    }
    public InductionCookerBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.INDUCTION_COOKER.get(), pos, state, BASE_SLOT_COUNT); }
    @Override public CookingStationKind getStationKind() { return CookingStationKind.INDUCTION; }
    public EnergyStorage getEnergyStorage() { return energy; }
    @Override protected boolean payForDish(CookingTier tier) {
        return energy.consume(switch (tier) { case SIMPLE -> 100; case COMPLEX -> 200; case REFINED -> 400; });
    }
    @Override public int getFuelAmount() { return energy.getEnergyStored(); }
    @Override protected int getFuelScaled(int scale) { return getFuelAmount() * scale / ENERGY_CAPACITY; }
    @Override protected void saveStationData(CompoundTag tag, HolderLookup.Provider registries) { tag.putInt("Energy", energy.getEnergyStored()); }
    @Override protected void loadStationData(CompoundTag tag, HolderLookup.Provider registries) { energy.restore(tag.getInt("Energy")); }
}
