package dev.ftb.mods.ftbskies2aerocompanion.compat.jade;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.AeroScoopBlock;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.AeroScoopBlockEntity;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.MeshItem;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.MeshTier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@WailaPlugin(FTBSkies2AeroCompanion.MOD_ID)
public class AeroScoopJadePlugin implements IWailaPlugin {
    public static final ResourceLocation FILTER_UID = FTBSkies2AeroCompanion.id("aeroscoop_filter");
    public static final ResourceLocation OUTPUT_UID = FTBSkies2AeroCompanion.id("aeroscoop_output");

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new FilterProvider(), AeroScoopBlock.class);
        registration.registerBlockComponent(new OutputProvider(), AeroScoopBlock.class);
        registration.addConfig(FILTER_UID, true);
        registration.addConfig(OUTPUT_UID, true);
    }

    private static final class FilterProvider implements IBlockComponentProvider {
        @Override
        public ResourceLocation getUid() {
            return FILTER_UID;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (!(accessor.getBlockEntity() instanceof AeroScoopBlockEntity be)) return;
            ItemStack filter = be.getFilter();
            IElementHelper helper = IElementHelper.get();
            if (filter.isEmpty()) {
                tooltip.add(Component.translatable("jade." + FTBSkies2AeroCompanion.MOD_ID + ".aeroscoop.no_filter")
                        .withStyle(ChatFormatting.GRAY));
                return;
            }
            tooltip.add(helper.item(filter.copyWithCount(1)));
            tooltip.append(Component.literal(" ")
                    .append(filter.getHoverName())
                    .withStyle(ChatFormatting.WHITE));

            MeshTier tier = MeshTier.of(filter);
            if (tier != null) {
                String pct = String.format(Locale.ROOT, "%.0f%%", tier.efficiency * 100.0);
                tooltip.add(Component.translatable(
                        "jade." + FTBSkies2AeroCompanion.MOD_ID + ".aeroscoop.efficiency", pct
                ).withStyle(ChatFormatting.GRAY));
            }

            if (filter.isDamageableItem() && filter.getMaxDamage() > 0) {
                int remaining = filter.getMaxDamage() - filter.getDamageValue();
                tooltip.add(Component.translatable(
                        "jade." + FTBSkies2AeroCompanion.MOD_ID + ".aeroscoop.durability",
                        remaining, filter.getMaxDamage()
                ).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }

    private static final class OutputProvider implements IBlockComponentProvider {
        @Override
        public ResourceLocation getUid() {
            return OUTPUT_UID;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (!(accessor.getBlockEntity() instanceof AeroScoopBlockEntity be)) return;
            Map<ItemStack, Integer> totals = new HashMap<>();
            int totalCount = 0;
            for (int i = 0; i < be.outputHandler.getSlots(); i++) {
                ItemStack slotStack = be.outputHandler.getStackInSlot(i);
                if (slotStack.isEmpty()) continue;
                totalCount += slotStack.getCount();
                ItemStack key = null;
                for (ItemStack k : totals.keySet()) {
                    if (ItemStack.isSameItemSameComponents(k, slotStack)) {
                        key = k;
                        break;
                    }
                }
                if (key == null) {
                    totals.put(slotStack.copyWithCount(1), slotStack.getCount());
                } else {
                    totals.merge(key, slotStack.getCount(), Integer::sum);
                }
            }

            if (totalCount == 0) {
                tooltip.add(Component.translatable("jade." + FTBSkies2AeroCompanion.MOD_ID + ".aeroscoop.empty_buffer")
                        .withStyle(ChatFormatting.DARK_GRAY));
                return;
            }

            tooltip.add(Component.translatable(
                    "jade." + FTBSkies2AeroCompanion.MOD_ID + ".aeroscoop.buffer_header", totalCount
            ).withStyle(ChatFormatting.GRAY));

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
}
