package dev.ftb.mods.ftbskies2aerocompanion.compat.createnewage;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.antarcticgardens.cna.content.electricity.wire.ElectricWireItem;
import org.antarcticgardens.cna.content.electricity.wire.WireType;
import org.antarcticgardens.cna.content.energising.EnergisingBlockItem;

import java.util.EnumMap;
import java.util.Map;

public final class CnaAddon {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(FTBSkies2AeroCompanion.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FTBSkies2AeroCompanion.MOD_ID);

    public enum Energiser {
        NETHERITE("netherite_energiser", CnaAddonConfig.TIER_NETHERITE, MapColor.COLOR_BLACK),
        PLATINUM("platinum_energiser", CnaAddonConfig.TIER_PLATINUM, MapColor.SNOW),
        TITANIUM("titanium_energiser", CnaAddonConfig.TIER_TITANIUM, MapColor.COLOR_GRAY);

        public final String id;
        public final int tier;
        public final MapColor mapColor;

        Energiser(String id, int tier, MapColor mapColor) {
            this.id = id;
            this.tier = tier;
            this.mapColor = mapColor;
        }
    }

    public enum WireMaterial {
        COPPER("copper", MapColor.COLOR_ORANGE),
        NETHERITE("netherite", MapColor.COLOR_BLACK),
        PLATINUM("platinum", MapColor.SNOW),
        TITANIUM("titanium", MapColor.COLOR_GRAY);

        public final String material;
        public final MapColor mapColor;

        WireMaterial(String material, MapColor mapColor) {
            this.material = material;
            this.mapColor = mapColor;
        }

        public String wireId() {
            return "overcharged_" + material + "_wire";
        }

        public String wireBlockId() {
            return "overcharged_" + material + "_wire_block";
        }

        public String sheetId() {
            return "overcharged_" + material + "_sheet";
        }

        public String wireTypeName() {
            return "OVERCHARGED_" + material.toUpperCase();
        }
    }

    public static final Map<Energiser, DeferredBlock<AeroEnergiserBlock>> ENERGISERS = new EnumMap<>(Energiser.class);
    public static final Map<WireMaterial, DeferredItem<Item>> WIRES = new EnumMap<>(WireMaterial.class);
    public static final Map<WireMaterial, DeferredBlock<RotatedPillarBlock>> WIRE_BLOCKS = new EnumMap<>(WireMaterial.class);
    public static final Map<WireMaterial, DeferredItem<Item>> SHEETS = new EnumMap<>(WireMaterial.class);

    static {
        for (Energiser e : Energiser.values()) {
            DeferredBlock<AeroEnergiserBlock> block = BLOCKS.register(e.id, () -> new AeroEnergiserBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(e.mapColor)
                            .requiresCorrectToolForDrops()
                            .noOcclusion(),
                    e.tier));
            ENERGISERS.put(e, block);
            ITEMS.register(e.id, () -> new EnergisingBlockItem(block.get(), new Item.Properties()));
        }

        for (WireMaterial m : WireMaterial.values()) {
            WIRES.put(m, ITEMS.register(m.wireId(), () -> makeWire(m)));

            DeferredBlock<RotatedPillarBlock> wireBlock = BLOCKS.register(m.wireBlockId(),
                    () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                            .mapColor(m.mapColor)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()
                            .sound(SoundType.COPPER)));
            WIRE_BLOCKS.put(m, wireBlock);
            ITEMS.register(m.wireBlockId(), () -> new BlockItem(wireBlock.get(), new Item.Properties()));

            SHEETS.put(m, ITEMS.register(m.sheetId(), () -> new Item(new Item.Properties())));
        }
    }

    private CnaAddon() {}

    private static Item makeWire(WireMaterial m) {
        try {
            return new ElectricWireItem(new Item.Properties(), WireType.valueOf(m.wireTypeName()));
        } catch (IllegalArgumentException missingWireType) {
            return new Item(new Item.Properties());
        }
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }

    public static void addToCreativeTab(CreativeModeTab.Output output) {
        for (Energiser e : Energiser.values()) {
            output.accept(ENERGISERS.get(e).get());
        }
        for (WireMaterial m : WireMaterial.values()) {
            output.accept(WIRES.get(m).get());
            output.accept(WIRE_BLOCKS.get(m).get());
            output.accept(SHEETS.get(m).get());
        }
    }

    public static boolean isAeroWire(String wireTypeName) {
        for (WireMaterial m : WireMaterial.values()) {
            if (m.wireTypeName().equals(wireTypeName)) {
                return true;
            }
        }
        return false;
    }

    public static ItemStack wireDropFor(String wireTypeName) {
        for (WireMaterial m : WireMaterial.values()) {
            if (m.wireTypeName().equals(wireTypeName)) {
                Item item = BuiltInRegistries.ITEM.get(FTBSkies2AeroCompanion.id(m.wireId()));
                return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
            }
        }
        return ItemStack.EMPTY;
    }
}
