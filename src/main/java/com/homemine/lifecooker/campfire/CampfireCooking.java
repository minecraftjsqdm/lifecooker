package com.homemine.lifecooker.campfire;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * 原版营火改造的注册与事件入口。
 * <p>
 * 营火自身不含模组数据，故烹饪推进走 {@link LevelTickEvent.Post}（原版
 * {@code CampfireBlockEntity} 的 ticker 已被原版逻辑占用）。
 */
public final class CampfireCooking {
    private CampfireCooking() {
    }

    /** 为原版营火注册 {@code IItemHandler}，使漏斗 / Pipez 可插入食材、抽出产物。 */
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, com.homemine.lifecooker.registry.ModBlockEntities.GRILL.get(), (be, side) -> be.getItemHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, com.homemine.lifecooker.registry.ModBlockEntities.STOVE.get(), (be, side) -> be.getItemHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, com.homemine.lifecooker.registry.ModBlockEntities.INDUCTION_COOKER.get(), (be, side) -> be.getItemHandler());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, com.homemine.lifecooker.registry.ModBlockEntities.STOVE.get(), (be, side) -> be.getFluidTank());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, com.homemine.lifecooker.registry.ModBlockEntities.INDUCTION_COOKER.get(), (be, side) -> be.getEnergyStorage());
        event.registerBlock(Capabilities.ItemHandler.BLOCK, (level, pos, state, blockEntity, side) -> {
            if (!(level instanceof ServerLevel serverLevel)) {
                return null;
            }
            return CampfireCookingData.get(serverLevel).getOrCreate(pos).getItemHandler();
        }, Blocks.CAMPFIRE);
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            CampfireCookingData.get(serverLevel).tick(serverLevel);
        }
    }

    /**
     * 右键营火打开烹饪界面。
     * <p>
     * 避让规则：手持原版可烤食物且原版 4 槽有空位时，交还原版烤制逻辑；
     * 其余情况（含空手、非可烤物品、原版槽已满）接管为打开烹饪界面。
     * 点燃 / 熄灭走 {@code canPerformAction}（打火石、火电荷、锹），不经本事件，故不受影响。
     */
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        if (!level.getBlockState(pos).is(Blocks.CAMPFIRE)) {
            return;
        }
        Player player = event.getEntity();
        if (player.isSpectator()) {
            return;
        }
        if (level.getBlockEntity(pos) instanceof CampfireBlockEntity campfire
                && campfire.getCookableRecipe(event.getItemStack()).isPresent()) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            CampfireCookingData.get(serverLevel).openMenu(serverPlayer, pos);
        }
    }
}
