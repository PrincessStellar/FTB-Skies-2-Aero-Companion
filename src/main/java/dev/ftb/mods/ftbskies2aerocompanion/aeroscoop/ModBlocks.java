package dev.ftb.mods.ftbskies2aerocompanion.aeroscoop;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(FTBSkies2AeroCompanion.MOD_ID);

    public static final DeferredBlock<AeroScoopBlock> AIR_FILTER = BLOCKS.register(
            "air_filter",
            () -> new AeroScoopBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion())
    );

    private ModBlocks() {}

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
