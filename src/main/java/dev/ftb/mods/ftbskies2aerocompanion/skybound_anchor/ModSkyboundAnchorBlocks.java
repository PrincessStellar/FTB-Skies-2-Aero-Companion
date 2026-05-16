package dev.ftb.mods.ftbskies2aerocompanion.skybound_anchor;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSkyboundAnchorBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(FTBSkies2AeroCompanion.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FTBSkies2AeroCompanion.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, FTBSkies2AeroCompanion.MOD_ID);

    public static final DeferredBlock<SkyboundAnchorBlock> SKYBOUND_ANCHOR = BLOCKS.register(
            "skybound_anchor",
            () -> new SkyboundAnchorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE)
                    .strength(3.5F, 6.0F)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops())
    );

    public static final DeferredItem<SkyboundAnchorItem> SKYBOUND_ANCHOR_ITEM = ITEMS.register(
            "skybound_anchor",
            () -> new SkyboundAnchorItem(SKYBOUND_ANCHOR.get(), new Item.Properties())
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SkyboundAnchorBlockEntity>> SKYBOUND_ANCHOR_BE =
            BLOCK_ENTITIES.register(
                    "skybound_anchor",
                    () -> BlockEntityType.Builder.of(SkyboundAnchorBlockEntity::new, SKYBOUND_ANCHOR.get()).build(null)
            );

    private ModSkyboundAnchorBlocks() {}

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
    }
}
