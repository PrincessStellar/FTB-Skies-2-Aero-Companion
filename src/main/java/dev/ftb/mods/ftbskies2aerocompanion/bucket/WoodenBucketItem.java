package dev.ftb.mods.ftbskies2aerocompanion.bucket;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

import java.util.List;

public class WoodenBucketItem extends Item {
    public WoodenBucketItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getFluid(stack).getAmount() > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        FluidStack fluid = getFluid(stack);
        return Math.round(13.0F * fluid.getAmount() / WoodenBucketFluidHandler.CAPACITY);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x3F76E4;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        FluidStack fluid = getFluid(stack);
        if (!fluid.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.ftbskies2aerocompanion.wooden_bucket.contents",
                    fluid.getHoverName(), fluid.getAmount(), WoodenBucketFluidHandler.CAPACITY));
        }
        tooltip.add(Component.translatable("tooltip.ftbskies2aerocompanion.wooden_bucket.fragile",
                Math.round(WoodenBucketFluidHandler.BREAK_CHANCE * 100))
                .withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    private static FluidStack getFluid(ItemStack stack) {
        SimpleFluidContent contents = stack.get(ModBucketComponents.WOODEN_BUCKET_CONTENTS.get());
        return contents == null ? FluidStack.EMPTY : contents.copy();
    }
}
