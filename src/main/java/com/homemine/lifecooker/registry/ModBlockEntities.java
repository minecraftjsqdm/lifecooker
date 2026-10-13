package com.homemine.lifecooker.registry;

import com.homemine.lifecooker.LifeCookerMod;
import com.homemine.lifecooker.blockentity.GrillBlockEntity;
import com.homemine.lifecooker.blockentity.InductionCookerBlockEntity;
import com.homemine.lifecooker.blockentity.StoveBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, LifeCookerMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GrillBlockEntity>> GRILL =
            BLOCK_ENTITIES.register("grill",
                    () -> BlockEntityType.Builder.of(GrillBlockEntity::new, ModBlocks.GRILL.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StoveBlockEntity>> STOVE =
            BLOCK_ENTITIES.register("stove",
                    () -> BlockEntityType.Builder.of(StoveBlockEntity::new, ModBlocks.STOVE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<InductionCookerBlockEntity>> INDUCTION_COOKER =
            BLOCK_ENTITIES.register("induction_cooker",
                    () -> BlockEntityType.Builder.of(InductionCookerBlockEntity::new, ModBlocks.INDUCTION_COOKER.get()).build(null));
}
