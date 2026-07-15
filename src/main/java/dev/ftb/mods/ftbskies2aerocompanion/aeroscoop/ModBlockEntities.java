package dev.ftb.mods.ftbskies2aerocompanion.aeroscoop;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, FTBSkies2AeroCompanion.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AeroScoopBlockEntity>> AIR_FILTER_BE =
            BLOCK_ENTITIES.register(
                    "air_filter",
                    () -> BlockEntityType.Builder.of(AeroScoopBlockEntity::new, ModBlocks.AIR_FILTER.get()).build(null)
            );

    private ModBlockEntities() {}

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
