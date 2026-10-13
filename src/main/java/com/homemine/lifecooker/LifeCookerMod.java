package com.homemine.lifecooker;

import com.homemine.lifecooker.campfire.CampfireCooking;
import com.homemine.lifecooker.data.ModAttachments;
import com.homemine.lifecooker.registry.ModBlockEntities;
import com.homemine.lifecooker.registry.ModBlocks;
import com.homemine.lifecooker.registry.ModCreativeTabs;
import com.homemine.lifecooker.registry.ModDataComponents;
import com.homemine.lifecooker.registry.ModDataMaps;
import com.homemine.lifecooker.registry.ModItems;
import com.homemine.lifecooker.registry.ModMenus;
import com.homemine.lifecooker.registry.ModRecipes;
import com.homemine.lifecooker.registry.ModTags;
import com.homemine.lifecooker.trait.TraitEffects;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(LifeCookerMod.MODID)
public class LifeCookerMod {
    public static final String MODID = "lifecooker";

    public LifeCookerMod(IEventBus modBus) {
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        ModMenus.MENUS.register(modBus);
        ModRecipes.RECIPE_TYPES.register(modBus);
        ModRecipes.RECIPE_SERIALIZERS.register(modBus);
        ModDataComponents.DATA_COMPONENTS.register(modBus);
        ModAttachments.ATTACHMENT_TYPES.register(modBus);

        modBus.addListener(ModCreativeTabs::onBuildContents);
        modBus.addListener(ModDataMaps::onRegisterDataMapTypes);
        modBus.addListener(CampfireCooking::registerCapabilities);

        NeoForge.EVENT_BUS.addListener(CampfireCooking::onLevelTick);
        NeoForge.EVENT_BUS.addListener(CampfireCooking::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(ModTags::onItemTooltip);
        NeoForge.EVENT_BUS.addListener(TraitEffects::onItemUseFinish);
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)
                com.homemine.lifecooker.data.MachineCookingData.get(player.serverLevel()).deliver(player);
        });
    }
}
