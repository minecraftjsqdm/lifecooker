package com.homemine.lifecooker.mixin;

import com.homemine.lifecooker.campfire.CampfireCookingData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 让原版营火向红石比较器输出模组侧的输入信号（每个已放入食材的输入槽 +2 强度）。
 * <p>
 * 两个方法声明在 {@link BlockBehaviour}，原版营火并未覆写，因此这里注入基类并按方块类型过滤。
 */
@Mixin(BlockBehaviour.class)
public abstract class CampfireComparatorMixin {
    @Inject(method = "hasAnalogOutputSignal", at = @At("HEAD"), cancellable = true)
    private void lifecooker$hasSignal(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.is(Blocks.CAMPFIRE)) cir.setReturnValue(true);
    }

    @Inject(method = "getAnalogOutputSignal", at = @At("HEAD"), cancellable = true)
    private void lifecooker$signal(BlockState state, Level level, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if (state.is(Blocks.CAMPFIRE) && level instanceof ServerLevel server) {
            cir.setReturnValue(CampfireCookingData.get(server).getOrCreate(pos).getInputSignal());
        }
    }
}
