package dev.ftb.mods.ftbskies2aerocompanion.compat.oritech;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class OritechCatalystConfig {
    public static final int DEFAULT_MAX_OVERENCHANT_LEVEL = 15;

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue MAX_OVERENCHANT_LEVEL;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("enchantment_catalyst");
        MAX_OVERENCHANT_LEVEL = b
                .comment("Highest enchantment level the Oritech Enchantment Catalyst can overenchant an item to. Vanilla's hard ceiling is 255; this caps it lower.")
                .defineInRange("max_overenchant_level", DEFAULT_MAX_OVERENCHANT_LEVEL, 1, 255);
        b.pop();
        SPEC = b.build();
    }

    private OritechCatalystConfig() {}

    public static int maxOverenchantLevel() {
        if (!SPEC.isLoaded()) {
            return DEFAULT_MAX_OVERENCHANT_LEVEL;
        }
        return MAX_OVERENCHANT_LEVEL.get();
    }
}
