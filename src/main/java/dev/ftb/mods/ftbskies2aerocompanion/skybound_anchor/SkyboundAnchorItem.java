package dev.ftb.mods.ftbskies2aerocompanion.skybound_anchor;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class SkyboundAnchorItem extends BlockItem {
    public SkyboundAnchorItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(net.minecraft.world.item.ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.ftbskies2aerocompanion.skybound_anchor.line1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.ftbskies2aerocompanion.skybound_anchor.line2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.ftbskies2aerocompanion.skybound_anchor.line3").withStyle(ChatFormatting.DARK_GRAY));
    }
}
