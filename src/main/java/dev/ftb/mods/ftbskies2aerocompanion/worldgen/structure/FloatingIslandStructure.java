package dev.ftb.mods.ftbskies2aerocompanion.worldgen.structure;

import com.mojang.serialization.MapCodec;
import dev.ftb.mods.ftbskies2aerocompanion.worldgen.feature.IslandPlacementResolver;
import dev.ftb.mods.ftbskies2aerocompanion.worldgen.registry.ModFeatures;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

public class FloatingIslandStructure extends Structure {
    public static final MapCodec<FloatingIslandStructure> CODEC = simpleCodec(FloatingIslandStructure::new);

    public FloatingIslandStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        return IslandPlacementResolver.resolve(context.seed(), chunkPos, context.registryAccess())
                .map(island -> new GenerationStub(island.center(), builder ->
                        builder.addPiece(new FloatingIslandPiece(island.center(), island.radius(),
                                island.seedTag(), island.biomeKey(), island.reach()))));
    }

    @Override
    public StructureType<?> type() {
        return ModFeatures.FLOATING_ISLAND_STRUCTURE.get();
    }
}
