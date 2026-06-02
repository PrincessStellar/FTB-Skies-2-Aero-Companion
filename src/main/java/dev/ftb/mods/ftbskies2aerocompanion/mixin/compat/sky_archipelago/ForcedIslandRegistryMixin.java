package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.sky_archipelago;

import dev.ftb.mods.ftbskies2aerocompanion.compat.sa.ForcedArchetypeContext;
import org.objectweb.asm.Opcodes;
import org.sathrek.sky_archipelago.worldgen.generator.field.IslandShapeArchetype;
import org.sathrek.sky_archipelago.worldgen.generator.field.forced.ForcedIslandRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(value = ForcedIslandRegistry.class, remap = false)
public abstract class ForcedIslandRegistryMixin {
    @Redirect(
            method = "injectForcedHostIsland",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.GETSTATIC,
                    target = "Lorg/sathrek/sky_archipelago/worldgen/generator/field/IslandShapeArchetype;CLASSIC:Lorg/sathrek/sky_archipelago/worldgen/generator/field/IslandShapeArchetype;"
            ),
            require = 0,
            expect = 0
    )
    private IslandShapeArchetype ftbskies2aero$forcedArchetype() {
        return ForcedArchetypeContext.resolve(IslandShapeArchetype.CLASSIC);
    }
}
