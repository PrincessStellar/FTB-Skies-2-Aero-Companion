package dev.ftb.mods.ftbskies2aerocompanion.worldgen.registry;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import dev.ftb.mods.ftbskies2aerocompanion.worldgen.structure.FloatingIslandPiece;
import dev.ftb.mods.ftbskies2aerocompanion.worldgen.structure.FloatingIslandStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModFeatures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, FTBSkies2AeroCompanion.MOD_ID);

    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, FTBSkies2AeroCompanion.MOD_ID);

    public static final DeferredHolder<StructureType<?>, StructureType<FloatingIslandStructure>>
            FLOATING_ISLAND_STRUCTURE = STRUCTURE_TYPES.register(
                    "floating_island",
                    () -> () -> FloatingIslandStructure.CODEC
            );

    public static final DeferredHolder<StructurePieceType, StructurePieceType>
            FLOATING_ISLAND_PIECE = STRUCTURE_PIECE_TYPES.register(
                    "floating_island_piece",
                    () -> (StructurePieceType) FloatingIslandPiece::new
            );

    public static void register(IEventBus bus) {
        STRUCTURE_TYPES.register(bus);
        STRUCTURE_PIECE_TYPES.register(bus);
    }

    private ModFeatures() {}
}
