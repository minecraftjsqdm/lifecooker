package com.homemine.lifecooker.trait;

import com.homemine.lifecooker.cook.FoodTrait;
import com.homemine.lifecooker.registry.ModDataComponents;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

import java.util.List;

/**
 * 特性效果应用（spec §8.2）：食用携带特性组件的物品后，为其施加对应的原版效果。
 * <p>
 * 效果等级取 {@link FoodTrait#value()}（1 为 I 级），持续时间取 {@link FoodTrait#duration()} 秒。
 */
public final class TraitEffects {
    private TraitEffects() {}

    /** @return 物品栈上携带的特性；未携带时为空列表。 */
    public static List<FoodTrait> foodTraits(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.FOOD_TRAITS.get(), List.of());
    }

    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        ItemStack stack = event.getItem();
        if (stack.isEmpty()) {
            return;
        }
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        for (FoodTrait trait : foodTraits(stack)) {
            TraitType type = TraitType.byId(trait.type());
            if (type == null || trait.duration() <= 0) {
                continue;
            }
            Holder<MobEffect> effect = type.effect();
            if (effect == null) {
                // 经验加成 / 抗寒 / 抗热：首期仅作标记，无原版效果可施加（spec §16-7）。
                continue;
            }
            entity.addEffect(new MobEffectInstance(effect, trait.duration() * 20, Math.max(0, trait.value() - 1)));
        }
    }
}
