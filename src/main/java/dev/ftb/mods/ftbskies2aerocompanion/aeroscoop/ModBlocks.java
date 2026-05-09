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
    public static final DeferredRegister.Blocks FTB_BLOCKS = DeferredRegister.createBlocks("ftb");

    public static final DeferredBlock<AeroScoopBlock> AIR_FILTER = BLOCKS.register(
            "air_filter",
            () -> new AeroScoopBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0F, 6.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion())
    );

    public static final DeferredBlock<Block> IRON_DUST = registerDust("iron_dust", MapColor.METAL);
    public static final DeferredBlock<Block> GOLD_DUST = registerDust("gold_dust", MapColor.GOLD);
    public static final DeferredBlock<Block> DIAMOND_DUST = registerDust("diamond_dust", MapColor.DIAMOND);
    public static final DeferredBlock<Block> BLAZING_DUST = registerDust("blazing_dust", MapColor.COLOR_ORANGE);

    private static DeferredBlock<Block> registerDust(String name, MapColor color) {
        return FTB_BLOCKS.registerSimpleBlock(
                name,
                BlockBehaviour.Properties.of()
                        .mapColor(color)
                        .strength(0.5F)
                        .sound(SoundType.SAND)
        );
    }

    private ModBlocks() {}

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        FTB_BLOCKS.register(bus);
    }
}
