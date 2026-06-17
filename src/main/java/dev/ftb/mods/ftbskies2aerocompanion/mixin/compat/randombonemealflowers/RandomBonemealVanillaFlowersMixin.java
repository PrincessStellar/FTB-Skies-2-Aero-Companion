package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.randombonemealflowers;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(targets = "com.natamus.randombonemealflowers_common_neoforge.util.Util", remap = false)
public class RandomBonemealVanillaFlowersMixin {

    @Shadow
    public static List<Block> flowers;

    @Inject(method = "setFlowerList", at = @At("TAIL"))
    private static void ftbskies2aero$vanillaFlowersOnly(Level level, CallbackInfo ci) {
        if (flowers != null) {
            flowers.removeIf(block -> !BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals("minecraft"));
        }
    }
}
