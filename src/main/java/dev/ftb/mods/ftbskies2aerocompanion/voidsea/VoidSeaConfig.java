package dev.ftb.mods.ftbskies2aerocompanion.voidsea;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class VoidSeaConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.DoubleValue SEA_LEVEL;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("void_sea");
        ENABLED = b
                .comment("When true, Sable sub-levels (airships) that fall below the void sea level are caught and held at the surface instead of sinking forever. Applies in every dimension, including the Nether.")
                .define("enabled", true);
        SEA_LEVEL = b
                .comment(
                        "World Y coordinate of the void sea surface. A ship that drops below this is lifted back to it and its downward velocity is cancelled; upward and horizontal motion are left alone so it can still be flown away.",
                        "Keep this above Forgiving Void's player trigger (dimension min build height minus its triggerAtDistanceBelow) so a rider is caught with the ship rather than being yanked off it first."
                )
                .defineInRange("sea_level", -32.0, -4096.0, 320.0);
        b.pop();
        SPEC = b.build();
    }

    private VoidSeaConfig() {}
}
