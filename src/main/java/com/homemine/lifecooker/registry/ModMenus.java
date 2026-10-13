package com.homemine.lifecooker.registry;

import com.homemine.lifecooker.LifeCookerMod;
import com.homemine.lifecooker.menu.CookingMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, LifeCookerMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<CookingMenu>> COOKING =
            MENUS.register("cooking", () -> IMenuTypeExtension.create(CookingMenu::new));
}
