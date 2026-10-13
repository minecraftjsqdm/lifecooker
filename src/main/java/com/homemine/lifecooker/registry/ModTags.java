package com.homemine.lifecooker.registry;

import com.homemine.lifecooker.LifeCookerMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** 「料理台」归属标签：烧烤炉 / 家用灶炉 / 电磁炉 / 原版营火。 */
public final class ModTags {
    public static final TagKey<Block> COOKING_STATIONS_BLOCK = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(LifeCookerMod.MODID, "cooking_stations"));
    public static final TagKey<Item> COOKING_STATIONS_ITEM = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(LifeCookerMod.MODID, "cooking_stations"));

    private ModTags() {}

    /** 属于料理台标签的物品，悬停时追加一行 {@code #料理台}。 */
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!event.getItemStack().is(COOKING_STATIONS_ITEM)) {
            return;
        }
        event.getToolTip().add(Component.translatable("tooltip.lifecooker.cooking_station")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
