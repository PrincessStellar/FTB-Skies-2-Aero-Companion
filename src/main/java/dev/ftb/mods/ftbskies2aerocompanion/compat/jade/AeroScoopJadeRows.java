package dev.ftb.mods.ftbskies2aerocompanion.compat.jade;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.MeshTier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.ITooltip;
import snownee.jade.api.ui.IElementHelper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class AeroScoopJadeRows {
    private static final String LANG_PREFIX = "jade." + FTBSkies2AeroCompanion.MOD_ID + ".aeroscoop.";

    private AeroScoopJadeRows() {}

    public static void appendFilter(ITooltip tooltip, ItemStack filter) {
        IElementHelper helper = IElementHelper.get();
        if (filter.isEmpty()) {
            tooltip.add(Component.translatable(LANG_PREFIX + "no_filter").withStyle(ChatFormatting.GRAY));
            return;
        }
        tooltip.add(helper.item(filter.copyWithCount(1)));
        tooltip.append(Component.literal(" ").append(filter.getHoverName()).withStyle(ChatFormatting.WHITE));

        MeshTier tier = MeshTier.of(filter);
        if (tier != null) {
            String pct = String.format(Locale.ROOT, "%.0f%%", tier.efficiency * 100.0);
            tooltip.add(Component.translatable(LANG_PREFIX + "efficiency", pct).withStyle(ChatFormatting.GRAY));
        }

        if (filter.isDamageableItem() && filter.getMaxDamage() > 0) {
            int remaining = filter.getMaxDamage() - filter.getDamageValue();
            tooltip.add(Component.translatable(LANG_PREFIX + "durability", remaining, filter.getMaxDamage())
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    public static void appendOutputs(ITooltip tooltip, List<ItemStack> stacks) {
        Map<ItemStack, Integer> totals = new LinkedHashMap<>();
        int totalCount = 0;
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) continue;
            totalCount += stack.getCount();
            ItemStack key = null;
            for (ItemStack k : totals.keySet()) {
                if (ItemStack.isSameItemSameComponents(k, stack)) {
                    key = k;
                    break;
                }
            }
            if (key == null) {
                totals.put(stack.copyWithCount(1), stack.getCount());
            } else {
                totals.merge(key, stack.getCount(), Integer::sum);
            }
        }

        if (totalCount == 0) {
            tooltip.add(Component.translatable(LANG_PREFIX + "empty_buffer").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        tooltip.add(Component.translatable(LANG_PREFIX + "buffer_header", totalCount).withStyle(ChatFormatting.GRAY));

        IElementHelper helper = IElementHelper.get();
        int shown = 0;
        for (Map.Entry<ItemStack, Integer> entry : totals.entrySet()) {
            if (shown >= 6) break;
            ItemStack copy = entry.getKey().copyWithCount(entry.getValue());
            tooltip.add(helper.smallItem(copy));
            tooltip.append(Component.literal(" " + entry.getValue() + "x ")
                    .append(entry.getKey().getHoverName()).withStyle(ChatFormatting.WHITE));
            shown++;
        }
    }
}
