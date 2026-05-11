package dev.ftb.mods.ftbskies2aerocompanion.bucket;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;

public class WoodenBucketFluidHandler extends FluidHandlerItemStack {
    public static final int CAPACITY = 1000;
    public static final float BREAK_CHANCE = 0.25F;

    private static final RandomSource RNG = RandomSource.create();

    public WoodenBucketFluidHandler(ItemStack container) {
        super(ModBucketComponents.WOODEN_BUCKET_CONTENTS, container, CAPACITY);
    }

    @Override
    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        FluidStack drained = super.drain(maxDrain, action);
        if (action.execute() && !drained.isEmpty() && getFluid().isEmpty() && RNG.nextFloat() < BREAK_CHANCE) {
            container.shrink(container.getCount());
        }
        return drained;
    }

    @Override
    public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        FluidStack drained = super.drain(resource, action);
        if (action.execute() && !drained.isEmpty() && getFluid().isEmpty() && RNG.nextFloat() < BREAK_CHANCE) {
            container.shrink(container.getCount());
        }
        return drained;
    }
}
