package dev.ftb.mods.ftbskies2aerocompanion.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.ftb.mods.ftbskies2aerocompanion.worldgen.feature.IslandStructureExclusion;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StructurePlacement.class)
public abstract class StructurePlacementIslandExclusionMixin {

    @ModifyReturnValue(method = "isStructureChunk(Lnet/minecraft/world/level/chunk/ChunkGeneratorStructureState;II)Z", at = @At("RETURN"))
    private boolean ftbskies2aero$excludeNearIslands(boolean original, ChunkGeneratorStructureState state, int x, int z) {
        if (!original) {
            return false;
        }
        StructurePlacement self = (StructurePlacement) (Object) this;
        if (!IslandStructureExclusion.tracks(self)) {
            return true;
        }
        try {
            if (IslandStructureExclusion.isChunkNearIsland(state.getLevelSeed(), x, z)) {
                return false;
            }
        } catch (Throwable ignored) {
        }
        return true;
    }
}
