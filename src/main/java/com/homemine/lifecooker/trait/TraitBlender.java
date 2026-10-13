package com.homemine.lifecooker.trait;

import com.homemine.lifecooker.cook.FoodTrait;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 「拼合弱化特性」计算（spec §8.3）：不可名状的食物由投入食材的特性拼合而来。
 * <p>
 * 规则：收集全部特性 → 同类求和 → 整体 ×0.5（向上取整、至少 1）→ 持续时间取各来源中较短值。
 */
public final class TraitBlender {
    private TraitBlender() {}

    public static List<FoodTrait> blend(List<FoodTrait> sources) {
        Map<ResourceLocation, Integer> valueSums = new LinkedHashMap<>();
        Map<ResourceLocation, Integer> minDurations = new LinkedHashMap<>();
        for (FoodTrait trait : sources) {
            valueSums.merge(trait.type(), trait.value(), Integer::sum);
            minDurations.merge(trait.type(), trait.duration(), Math::min);
        }

        List<FoodTrait> blended = new ArrayList<>(valueSums.size());
        valueSums.forEach((type, sum) -> {
            int weakened = Math.max(1, (int) Math.ceil(sum * 0.5D));
            blended.add(new FoodTrait(type, weakened, minDurations.get(type)));
        });
        // 按 ID 归一排序：同一批食材无论投入槽位顺序如何，产出的组件都一致（可叠加）。
        blended.sort(Comparator.comparing(trait -> trait.type().toString()));
        return blended;
    }
}
