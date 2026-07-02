package dev.ftb.mods.ftbskies2aerocompanion.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.logging.LogUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StructureStart.class)
public abstract class StructureStartPlaceGuardMixin {
    private static final Logger FTBSKIES2AERO$LOGGER = LogUtils.getLogger();

    @WrapMethod(method = "placeInChunk")
    private void ftbskies2aero$guardPlaceInChunk(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox box, ChunkPos chunkPos, Operation<Void> original) {
        try {
            original.call(level, structureManager, generator, random, box, chunkPos);
        } catch (Throwable t) {
            StructureStart self = (StructureStart) (Object) this;
            FTBSKIES2AERO$LOGGER.error(
                    "Structure {} threw while placing into chunk {} (box {}). Rethrowing so world generation is unchanged; this line just captures the cause for a targeted fix.",
                    self.getStructure(), chunkPos, box, t);
            if (t instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (t instanceof Error error) {
                throw error;
            }
            throw new RuntimeException(t);
        }
    }
}
