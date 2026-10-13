package com.homemine.lifecooker.registry;

import net.minecraft.world.inventory.RecipeBookType;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

/**
 * 配方书类型（{@link RecipeBookType}）的枚举扩展入口。
 * <p>
 * 新常量由 {@code META-INF/enumextensions.json} 在运行时注入，编译期并不存在，
 * 因此这里只能通过 {@link EnumProxy} 持有并在运行时取回。
 */
public final class ModRecipeBookTypes {
    /** 烹饪配方书类型，对应 enumextensions.json 中的 {@code LIFECOOKER_COOKING}。 */
    public static final EnumProxy<RecipeBookType> COOKING = new EnumProxy<>(RecipeBookType.class);

    private ModRecipeBookTypes() {
    }

    public static RecipeBookType cooking() {
        return COOKING.getValue();
    }
}
