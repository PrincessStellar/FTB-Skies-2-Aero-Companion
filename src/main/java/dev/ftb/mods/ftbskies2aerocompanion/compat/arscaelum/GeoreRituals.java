package dev.ftb.mods.ftbskies2aerocompanion.compat.arscaelum;

import com.hollingsworth.arsnouveau.api.registry.RitualRegistry;
import com.hollingsworth.arsnouveau.common.items.RitualTablet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

public class GeoreRituals {

    private static final String[] OVERWORLD_ORES = {
        "aluminum", "black_quartz", "coal", "copper", "gold", "iron",
        "lapis", "lead", "monazite", "nickel", "osmium", "redstone",
        "ruby", "sapphire", "silver", "tin", "topaz", "uranium", "zinc"
    };

    private static final String[] NETHER_ORES = {
        "ancient_debris", "diamond", "emerald", "platinum", "quartz", "tungsten"
    };

    public static final DeferredRegister.Items AC_ITEMS = DeferredRegister.createItems("ars_caelum");

    private static final List<GeoreIslandRitual> ALL_RITUALS = new ArrayList<>();

    static {
        for (String ore : OVERWORLD_ORES) {
            buildTablet(ore, false);
        }
        for (String ore : NETHER_ORES) {
            buildTablet(ore, true);
        }
    }

    private static void buildTablet(String ore, boolean nether) {
        String itemId = nether ? "ritual_conjure_island_nether_" + ore : "ritual_conjure_island_" + ore;
        ResourceLocation ritualId = ResourceLocation.fromNamespaceAndPath("ars_caelum", itemId);
        ResourceLocation structure = nether
            ? ResourceLocation.fromNamespaceAndPath("ftb", "geore_island_nether/" + ore)
            : ResourceLocation.fromNamespaceAndPath("ftb", "geore_island/" + ore);
        String langName = "Conjure Island: " + prettyName(ore) + (nether ? " (Nether)" : "");
        String langDesc = "Creates a " + prettyName(ore)
            + " geore island. Requires a full jar of Source to begin. NOTE: This ritual should be performed at least 14 blocks from any other block.";

        GeoreIslandRitual ritual = new GeoreIslandRitual(ritualId, structure, langName, langDesc);
        ALL_RITUALS.add(ritual);
        AC_ITEMS.register(itemId, () -> new RitualTablet(ritual));
    }

    private static String prettyName(String ore) {
        String[] parts = ore.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            for (GeoreIslandRitual ritual : ALL_RITUALS) {
                RitualRegistry.registerRitual(ritual);
                Item item = BuiltInRegistries.ITEM.get(ritual.getRegistryName());
                if (item instanceof RitualTablet tablet) {
                    RitualRegistry.getRitualItemMap().put(ritual.getRegistryName(), tablet);
                }
            }
        });
    }

    public static void register(IEventBus modBus) {
        AC_ITEMS.register(modBus);
        modBus.addListener(GeoreRituals::onCommonSetup);
    }
}
