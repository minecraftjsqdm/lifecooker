package com.homemine.lifecooker.block;

import com.homemine.lifecooker.blockentity.CookingStationBlockEntity;
import com.homemine.lifecooker.blockentity.InductionCookerBlockEntity;
import com.homemine.lifecooker.registry.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 电磁炉：以 FE 能量为燃料。
 */
public class InductionCookerBlock extends CookingStationBlock {
    public static final MapCodec<InductionCookerBlock> CODEC = simpleCodec(InductionCookerBlock::new);

    public InductionCookerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof CookingStationBlockEntity station) {
            station.openMenu(player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InductionCookerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide
                ? null
                : createTickerHelper(type, ModBlockEntities.INDUCTION_COOKER.get(), (lvl, pos, st, be) -> be.serverTick());
    }
}
