package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.integrateddynamics;

import dev.ftb.mods.ftbskies2aerocompanion.compat.integrateddynamics.IntegratedDynamicsNetworkReform;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.fml.ModList;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Structures place their blocks via {@code setBlock} without the neighbour updates a
 * normal placement does, so IntegratedDynamics cables come out with stale connections and
 * never link into a coherent network — the same failure Sable assembly hits. This covers
 * both structure blocks and worldgen, which both route through {@code placeInWorld}.
 *
 * <p>Structure-block / command placement runs on the server thread, so reform immediately.
 * Worldgen runs on the chunk-generation worker pool where ID network operations are
 * unsafe, so record the placed region and reform it on the server thread once it is fully
 * loaded. The cheap cable-presence check keeps non-ID structures (the worldgen norm) from
 * paying any cost.
 */
@Mixin(StructureTemplate.class)
public abstract class StructureTemplatePlaceMixin {

    @Inject(method = "placeInWorld", at = @At("RETURN"))
    private void ftbskies2aero$reformIdNetworks(ServerLevelAccessor levelAccessor, BlockPos pos, BlockPos pivot,
                                                StructurePlaceSettings settings, RandomSource random, int flags,
                                                CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() || !ModList.get().isLoaded("integrateddynamics")) {
            return;
        }
        StructureTemplate self = (StructureTemplate) (Object) this;
        Block cable = RegistryEntries.BLOCK_CABLE.get();
        if (self.filterBlocks(pos, settings, cable).isEmpty()) {
            return;
        }
        BoundingBox box = self.getBoundingBox(settings, pos);
        if (box == null) {
            return;
        }
        IntegratedDynamicsNetworkReform.recordDeferred(levelAccessor.getLevel(), box);
    }
}
