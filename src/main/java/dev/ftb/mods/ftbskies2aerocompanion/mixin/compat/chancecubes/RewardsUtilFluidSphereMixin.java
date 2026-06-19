package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.chancecubes;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Random;

@Mixin(targets = "chanceCubes.util.RewardsUtil", remap = false)
public class RewardsUtilFluidSphereMixin {

    private static final Random ftbskies2aero$RANDOM = new Random();

    @ModifyReturnValue(method = "getRandomFluid(Z)Lnet/minecraft/world/level/material/Fluid;", at = @At("RETURN"), remap = false)
    private static Fluid ftbskies2aero$ensurePlaceable(Fluid original) {
        if (ftbskies2aero$isPlaceable(original)) {
            return original;
        }
        List<Fluid> candidates = BuiltInRegistries.FLUID.stream()
                .filter(f -> f.defaultFluidState().isSource())
                .filter(RewardsUtilFluidSphereMixin::ftbskies2aero$isPlaceable)
                .toList();
        if (candidates.isEmpty()) {
            return Fluids.WATER;
        }
        return candidates.get(ftbskies2aero$RANDOM.nextInt(candidates.size()));
    }

    private static boolean ftbskies2aero$isPlaceable(Fluid fluid) {
        if (fluid == Fluids.EMPTY) {
            return false;
        }
        return !fluid.defaultFluidState().createLegacyBlock().isAir();
    }
}
