package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.mi;

import aztech.modern_industrialization.machines.components.FluidItemConsumerComponent;
import dev.ftb.mods.ftbskies2aerocompanion.compat.mi.MIMekSteam;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = FluidItemConsumerComponent.EuProductionMapBuilder.class, remap = false)
public class MIEuProductionMapBuilderMixin {

    @ModifyVariable(
            method = "add(Lnet/minecraft/resources/ResourceLocation;J)Laztech/modern_industrialization/machines/components/FluidItemConsumerComponent$EuProductionMapBuilder;",
            at = @At("HEAD"),
            argsOnly = true,
            index = 1
    )
    private ResourceLocation ftbskies$swapSteamKey(ResourceLocation key) {
        return MIMekSteam.swapId(key);
    }
}
