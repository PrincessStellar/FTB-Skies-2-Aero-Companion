package dev.ftb.mods.ftbskies2aerocompanion.worldgen.structure;

import dev.ftb.mods.ftbskies2aerocompanion.compat.sa.SkyAIslandBuilder;
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
    private static final int VERTICAL_HALO = 256;

    private final BlockPos center;
    private final int radius;
    private final long seedTag;
    private final ResourceKey<Biome> biomeKey;

    public FloatingIslandPiece(BlockPos center, int radius, long seedTag, ResourceKey<Biome> biomeKey) {
        super(ModFeatures.FLOATING_ISLAND_PIECE.get(), 0, computeBoundingBox(center, radius));
        this.center = center;
        this.radius = radius;
        this.seedTag = seedTag;
        this.biomeKey = biomeKey;
    }

    public FloatingIslandPiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(ModFeatures.FLOATING_ISLAND_PIECE.get(), tag);
        this.center = new BlockPos(tag.getInt("CX"), tag.getInt("CY"), tag.getInt("CZ"));
        this.radius = tag.getInt("R");
        this.seedTag = tag.getLong("Seed");
        this.biomeKey = ResourceKey.create(Registries.BIOME, ResourceLocation.parse(tag.getString("Biome")));
    }

    private static BoundingBox computeBoundingBox(BlockPos center, int radius) {
        int scan = radius + 64;
        return new BoundingBox(
                center.getX() - scan, center.getY() - VERTICAL_HALO, center.getZ() - scan,
                center.getX() + scan, center.getY() + VERTICAL_HALO, center.getZ() + scan
        );
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator,
                            RandomSource random, BoundingBox writeBox, ChunkPos chunkPos, BlockPos pivot) {
        SkyAIslandBuilder.buildSlice(level, random, center, radius, seedTag, biomeKey, writeBox);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        tag.putInt("CX", center.getX());
        tag.putInt("CY", center.getY());
        tag.putInt("CZ", center.getZ());
        tag.putInt("R", radius);
        tag.putLong("Seed", seedTag);
        tag.putString("Biome", biomeKey.location().toString());
    }
}
