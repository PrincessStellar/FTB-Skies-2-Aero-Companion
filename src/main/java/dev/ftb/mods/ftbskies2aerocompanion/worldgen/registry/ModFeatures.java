package dev.ftb.mods.ftbskies2aerocompanion.worldgen.registry;

import dev.ftb.mods.ftbskies2aerocompanion.worldgen.feature.FloatingIslandTerrainFeature;
import dev.ftb.mods.ftbskies2aerocompanion.worldgen.feature.IslandPlacementResolver;
import dev.ftb.mods.ftbskies2aerocompanion.worldgen.feature.IslandStructureExclusion;
import dev.ftb.mods.ftbskies2aerocompanion.worldgen.structure.FloatingIslandPiece;
import dev.ftb.mods.ftbskies2aerocompanion.worldgen.structure.FloatingIslandStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModFeatures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, "ftbskies2aerocompanion");
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECE_TYPES = DeferredRegister.create(Registries.STRUCTURE_PIECE, "ftbskies2aerocompanion");
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, "ftbskies2aerocompanion");

    public static final DeferredHolder<StructureType<?>, StructureType<FloatingIslandStructure>> FLOATING_ISLAND_STRUCTURE = STRUCTURE_TYPES.register("floating_island", () -> () -> FloatingIslandStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> FLOATING_ISLAND_PIECE = STRUCTURE_PIECE_TYPES.register("floating_island_piece", () -> FloatingIslandPiece::new);
    public static final DeferredHolder<Feature<?>, FloatingIslandTerrainFeature> FLOATING_ISLAND_TERRAIN = FEATURES.register("floating_island_terrain", () -> new FloatingIslandTerrainFeature(NoneFeatureConfiguration.CODEC));

    public static void register(IEventBus bus) {
        STRUCTURE_TYPES.register(bus);
        STRUCTURE_PIECE_TYPES.register(bus);
        FEATURES.register(bus);
        NeoForge.EVENT_BUS.addListener(IslandPlacementResolver::onServerStopped);
        NeoForge.EVENT_BUS.addListener(IslandStructureExclusion::onServerAboutToStart);
        NeoForge.EVENT_BUS.addListener(IslandStructureExclusion::onServerStopped);
    }

    private ModFeatures() {
    }
}
