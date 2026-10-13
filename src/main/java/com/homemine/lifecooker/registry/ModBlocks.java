package com.homemine.lifecooker.registry;

import com.homemine.lifecooker.LifeCookerMod;
import com.homemine.lifecooker.block.GrillBlock;
import com.homemine.lifecooker.block.InductionCookerBlock;
import com.homemine.lifecooker.block.StoveBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LifeCookerMod.MODID);

    /** 烧烤炉：固体燃料，金属炉体。模型非整方块（含外伸把手），需禁用遮挡剔除。 */
    public static final DeferredBlock<GrillBlock> GRILL = BLOCKS.registerBlock("grill", GrillBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5F, 6.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion());

    /** 家用灶炉：流体 / 气体燃料，陶瓷灶体。模型非整方块，需禁用遮挡剔除。 */
    public static final DeferredBlock<StoveBlock> STOVE = BLOCKS.registerBlock("stove", StoveBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.5F, 6.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion());

    /** 电磁炉：消耗 FE，玻璃面板。模型非整方块，需禁用遮挡剔除。 */
    public static final DeferredBlock<InductionCookerBlock> INDUCTION_COOKER = BLOCKS.registerBlock("induction_cooker", InductionCookerBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5F, 6.0F)
                    .sound(SoundType.GLASS)
                    .noOcclusion());
}
