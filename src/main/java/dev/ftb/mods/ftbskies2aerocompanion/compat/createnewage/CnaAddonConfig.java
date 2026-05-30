package dev.ftb.mods.ftbskies2aerocompanion.compat.createnewage;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class CnaAddonConfig {
    public static final int TIER_NETHERITE = 4;
    public static final int TIER_PLATINUM = 5;
    public static final int TIER_TITANIUM = 6;

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue NETHERITE_SPEED;
    public static final ModConfigSpec.LongValue NETHERITE_CAPACITY;
    public static final ModConfigSpec.DoubleValue NETHERITE_STRESS;

    public static final ModConfigSpec.IntValue PLATINUM_SPEED;
    public static final ModConfigSpec.LongValue PLATINUM_CAPACITY;
    public static final ModConfigSpec.DoubleValue PLATINUM_STRESS;

    public static final ModConfigSpec.IntValue TITANIUM_SPEED;
    public static final ModConfigSpec.LongValue TITANIUM_CAPACITY;
    public static final ModConfigSpec.DoubleValue TITANIUM_STRESS;

    public static final ModConfigSpec.LongValue COPPER_WIRE_CONDUCTIVITY;
    public static final ModConfigSpec.LongValue NETHERITE_WIRE_CONDUCTIVITY;
    public static final ModConfigSpec.LongValue PLATINUM_WIRE_CONDUCTIVITY;
    public static final ModConfigSpec.LongValue TITANIUM_WIRE_CONDUCTIVITY;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("Higher-tier energiser stats. Speed is FE/t generated per 10 RPM; capacity is total FE stored.").push("energisers");

        b.push("netherite");
        NETHERITE_SPEED = b.comment("FE/t per 10 RPM").defineInRange("speed", 256, 0, Integer.MAX_VALUE);
        NETHERITE_CAPACITY = b.comment("FE stored").defineInRange("capacity", 10_000_000L, 0L, Long.MAX_VALUE);
        NETHERITE_STRESS = b.comment("Stress impact in SU").defineInRange("stress", 64.0, 0.0, Double.MAX_VALUE);
        b.pop();

        b.push("platinum");
        PLATINUM_SPEED = b.comment("FE/t per 10 RPM").defineInRange("speed", 1024, 0, Integer.MAX_VALUE);
        PLATINUM_CAPACITY = b.comment("FE stored").defineInRange("capacity", 100_000_000L, 0L, Long.MAX_VALUE);
        PLATINUM_STRESS = b.comment("Stress impact in SU").defineInRange("stress", 128.0, 0.0, Double.MAX_VALUE);
        b.pop();

        b.push("titanium");
        TITANIUM_SPEED = b.comment("FE/t per 10 RPM").defineInRange("speed", 4096, 0, Integer.MAX_VALUE);
        TITANIUM_CAPACITY = b.comment("FE stored").defineInRange("capacity", 1_000_000_000L, 0L, Long.MAX_VALUE);
        TITANIUM_STRESS = b.comment("Stress impact in SU").defineInRange("stress", 256.0, 0.0, Double.MAX_VALUE);
        b.pop();

        b.pop();

        b.comment("Wire conductivity (max FE/t carried per connection before the conductivity multiplier).").push("wires");
        COPPER_WIRE_CONDUCTIVITY = b.defineInRange("overcharged_copper", 1536L, 0L, Long.MAX_VALUE);
        NETHERITE_WIRE_CONDUCTIVITY = b.defineInRange("overcharged_netherite", 16384L, 0L, Long.MAX_VALUE);
        PLATINUM_WIRE_CONDUCTIVITY = b.defineInRange("overcharged_platinum", 32768L, 0L, Long.MAX_VALUE);
        TITANIUM_WIRE_CONDUCTIVITY = b.defineInRange("overcharged_titanium", 65536L, 0L, Long.MAX_VALUE);
        b.pop();

        SPEC = b.build();
    }

    private CnaAddonConfig() {}

    public static int speedFor(int tier) {
        if (!SPEC.isLoaded()) {
            return (int) Math.pow(4, tier);
        }
        return switch (tier) {
            case TIER_NETHERITE -> NETHERITE_SPEED.get();
            case TIER_PLATINUM -> PLATINUM_SPEED.get();
            case TIER_TITANIUM -> TITANIUM_SPEED.get();
            default -> (int) Math.pow(4, tier);
        };
    }

    public static long capacityFor(int tier) {
        if (!SPEC.isLoaded()) {
            return (long) (Math.pow(10, tier) * 1000);
        }
        return switch (tier) {
            case TIER_NETHERITE -> NETHERITE_CAPACITY.get();
            case TIER_PLATINUM -> PLATINUM_CAPACITY.get();
            case TIER_TITANIUM -> TITANIUM_CAPACITY.get();
            default -> (long) (Math.pow(10, tier) * 1000);
        };
    }

    public static double stressFor(int tier) {
        if (!SPEC.isLoaded()) {
            return 32.0;
        }
        return switch (tier) {
            case TIER_NETHERITE -> NETHERITE_STRESS.get();
            case TIER_PLATINUM -> PLATINUM_STRESS.get();
            case TIER_TITANIUM -> TITANIUM_STRESS.get();
            default -> 32.0;
        };
    }

    public static long conductivityFor(String wireName) {
        if (!SPEC.isLoaded()) {
            return 1024L;
        }
        return switch (wireName) {
            case "OVERCHARGED_COPPER" -> COPPER_WIRE_CONDUCTIVITY.get();
            case "OVERCHARGED_NETHERITE" -> NETHERITE_WIRE_CONDUCTIVITY.get();
            case "OVERCHARGED_PLATINUM" -> PLATINUM_WIRE_CONDUCTIVITY.get();
            case "OVERCHARGED_TITANIUM" -> TITANIUM_WIRE_CONDUCTIVITY.get();
            default -> 1024L;
        };
    }
}
