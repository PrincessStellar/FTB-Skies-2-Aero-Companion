package dev.ftb.mods.ftbskies2aerocompanion.basebuffer;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class BaseExclusionConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue EXCLUSION_RADIUS;
    public static final ModConfigSpec.IntValue BASE_SIZE_REGIONS;
    public static final ModConfigSpec.IntValue BASE_SEPARATION_REGIONS;
    public static final ModConfigSpec.IntValue MAX_REGION_X;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("base_exclusion");
        EXCLUSION_RADIUS = b
                .comment("Block radius around each FTB Team Bases reserved region center where worldgen we control will be suppressed.")
                .defineInRange("exclusion_radius_blocks", 256, 0, 4096);
        BASE_SIZE_REGIONS = b
                .comment("Side length of a single base in 512-block regions. Must match FTB Team Bases base definition extents.x (default 1).")
                .defineInRange("base_size_regions", 1, 1, 16);
        BASE_SEPARATION_REGIONS = b
                .comment("Clear regions between adjacent bases. Must match FTB Team Bases server config 'base_separation'.")
                .defineInRange("base_separation_regions", 2, 0, 16);
        MAX_REGION_X = b
                .comment("Maximum region X before the allocator wraps to the next row. Mirrors BaseInstanceManager.MAX_REGION_X (default 2000).")
                .defineInRange("max_region_x", 2000, 1, 1000000);
        b.pop();
        SPEC = b.build();
    }

    private BaseExclusionConfig() {}
}
