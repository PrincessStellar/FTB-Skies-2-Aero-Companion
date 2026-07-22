package dev.ftb.mods.ftbskies2aerocompanion.worldgen.structure;

import dev.ftb.mods.ftbskies2aerocompanion.worldgen.registry.ModFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public class FloatingIslandPiece extends StructurePiece {
    private static final int MARKER_NEG_EXTENT = 8;
    private static final int MARKER_POS_EXTENT = 7;

    private final BlockPos center;
    private final int radius;
    private final long seedTag;
    private final ResourceKey<Biome> biomeKey;
    private final int reach;

    public FloatingIslandPiece(BlockPos center, int radius, long seedTag, ResourceKey<Biome> biomeKey, int reach) {
        super(ModFeatures.FLOATING_ISLAND_PIECE.get(), 0, computeBoundingBox(center));
        this.center = center;
        this.radius = radius;
        this.seedTag = seedTag;
        this.biomeKey = biomeKey;
        this.reach = reach;
    }

    public FloatingIslandPiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(ModFeatures.FLOATING_ISLAND_PIECE.get(), tag);
        this.center = new BlockPos(tag.getInt("CX"), tag.getInt("CY"), tag.getInt("CZ"));
        this.radius = tag.getInt("R");
        this.seedTag = tag.getLong("Seed");
        this.biomeKey = ResourceKey.create(Registries.BIOME, ResourceLocation.parse(tag.getString("Biome")));
        this.reach = tag.getInt("Reach");
    }

    private static BoundingBox computeBoundingBox(BlockPos center) {
        return new BoundingBox(
                center.getX() - MARKER_NEG_EXTENT, center.getY() - MARKER_NEG_EXTENT, center.getZ() - MARKER_NEG_EXTENT,
                center.getX() + MARKER_POS_EXTENT, center.getY() + MARKER_POS_EXTENT, center.getZ() + MARKER_POS_EXTENT
        );
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
                             RandomSource random, BoundingBox writeBox, ChunkPos chunkPos, BlockPos pivot) {
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        tag.putInt("CX", center.getX());
        tag.putInt("CY", center.getY());
        tag.putInt("CZ", center.getZ());
        tag.putInt("R", radius);
        tag.putLong("Seed", seedTag);
        tag.putString("Biome", biomeKey.location().toString());
        tag.putInt("Reach", reach);
    }
}
