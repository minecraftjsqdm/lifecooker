package com.homemine.lifecooker.block;

import com.homemine.lifecooker.blockentity.CookingStationBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockState;

public abstract class CookingStationBlock extends BaseEntityBlock {
    protected CookingStationBlock(Properties properties) { super(properties); }
    @Override protected boolean hasAnalogOutputSignal(BlockState state) { return true; }
    @Override protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof CookingStationBlockEntity station ? station.getInputSignal() : 0;
    }
    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
        if (!state.is(next.getBlock()) && level.getBlockEntity(pos) instanceof CookingStationBlockEntity station) {
            if (level instanceof net.minecraft.server.level.ServerLevel server) station.getQueue().drop(server, pos);
            net.minecraft.world.Containers.dropContents(level, pos, station);
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(state, level, pos, next, moving);
    }
}
