package dev.ftb.mods.ftbskies2aerocompanion.basebuffer;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class BaseExclusionConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue EXCLUSION_RADIUS;
    public static final ModConfigSpec.IntValue BASE_SIZE_REGIONS;
    public static final ModConfigSpec.IntValue BASE_SEPARATION_REGIONS;
    public static final ModConfigSpec.IntValue MAX_REGION_X;
    public static final ModConfigSpec.DoubleValue ISLAND_SPAWN_CHANCE;
    public static final ModConfigSpec.IntValue ISLAND_MIN_Y;
    public static final ModConfigSpec.IntValue ISLAND_MAX_Y;
    public static final ModConfigSpec.BooleanValue STRUCTURE_EXCLUSION_ENABLED;
    public static final ModConfigSpec.IntValue STRUCTURE_EXCLUSION_MARGIN;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> STRUCTURE_EXCLUSION_SETS;

    private BaseExclusionConfig() {
    }

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("base_exclusion");
        EXCLUSION_RADIUS = b.comment("Block radius around each FTB Team Bases reserved region center where worldgen we control will be suppressed.").defineInRange("exclusion_radius_blocks", 256, 0, 4096);
        BASE_SIZE_REGIONS = b.comment("Side length of a single base in 512-block regions. Must match FTB Team Bases base definition extents.x (default 1).").defineInRange("base_size_regions", 1, 1, 16);
        BASE_SEPARATION_REGIONS = b.comment("Clear regions between adjacent bases. Must match FTB Team Bases server config 'base_separation'.").defineInRange("base_separation_regions", 2, 0, 16);
        MAX_REGION_X = b.comment("Maximum region X before the allocator wraps to the next row. Mirrors BaseInstanceManager.MAX_REGION_X (default 2000).").defineInRange("max_region_x", 2000, 1, 1000000);
        b.pop();
        b.push("island_spawning");
        ISLAND_SPAWN_CHANCE = b.comment(
                "Chance (0.0-1.0) that each candidate floating island actually spawns. Applies ONLY to",
                "ftbskies2aerocompanion:floating_island terrain (and its locator marker) - special/structure",
                "islands from other mods (pillager outposts, valhelsia structures, etc.) are unaffected.",
                "1.0 generates worlds identical to a build without this option. Set BEFORE creating a world;",
                "changing it on an existing world only affects newly generated chunks and can leave partial",
                "islands at the old/new generation boundary.")
                .defineInRange("island_spawn_chance", 1.0, 0.0, 1.0);
        ISLAND_MIN_Y = b.comment(
                "Lowest center height (Y) a floating island can spawn at. Islands pick a center Y uniformly",
                "between island_min_y and island_max_y, so widening this range spreads islands across more",
                "altitudes. Above roughly Y 90 the game's height-adjusted temperature starts freezing exposed",
                "surfaces, so islands centered high tend to accumulate snow even in temperate biomes. Lower this",
                "value to let some islands sit low enough to stay green. Keep it high enough that islands still",
                "clear the terrain and sea below them. Set BEFORE creating a world; changing it only affects",
                "newly generated chunks.")
                .defineInRange("island_min_y", 80, -32, 320);
        ISLAND_MAX_Y = b.comment(
                "Highest center height (Y) a floating island can spawn at. Must be greater than or equal to",
                "island_min_y. Set BEFORE creating a world; changing it only affects newly generated chunks.")
                .defineInRange("island_max_y", 200, -32, 320);
        b.pop();
        b.push("structure_exclusion");
        STRUCTURE_EXCLUSION_ENABLED = b.comment(
                "When true, the structure sets listed in structure_sets are prevented from spawning on top of",
                "floating islands. Structures whose chosen chunk falls within an island's radius plus margin_blocks",
                "are skipped, leaving fewer sky structures near islands but no overlaps.")
                .define("enabled", true);
        STRUCTURE_EXCLUSION_MARGIN = b.comment(
                "Extra blocks of clearance beyond an island's own radius that a listed structure must keep away",
                "from the island center. Larger values push structures further from islands and reduce how many",
                "sky structures spawn overall. Should be at least as large as the biggest listed structure's reach",
                "so its edges cannot clip an island.")
                .defineInRange("margin_blocks", 128, 0, 2048);
        STRUCTURE_EXCLUSION_SETS = b.comment(
                "Structure set ids kept clear of floating islands. Defaults to the sky structure set. Ground",
                "structures do not need listing since they generate below island height.")
                .defineList("structure_sets", List.of("ftb:overworld_main"),
                        o -> o instanceof String s && ResourceLocation.tryParse(s) != null);
        b.pop();
        SPEC = b.build();
    }
}
