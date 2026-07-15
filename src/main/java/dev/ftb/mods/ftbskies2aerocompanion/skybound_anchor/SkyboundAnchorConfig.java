package dev.ftb.mods.ftbskies2aerocompanion.skybound_anchor;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class SkyboundAnchorConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue KP;
    public static final ModConfigSpec.DoubleValue KD;
    public static final ModConfigSpec.DoubleValue MAX_ANGULAR_ACCELERATION;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("skybound_anchor");
        KP = b
                .comment("Proportional gain (rad/s^2 per rad of tilt). Higher = stiffer / faster righting. Natural period ~= 2*pi/sqrt(kp).")
                .defineInRange("kp", 4.0, 0.0, 1000.0);
        KD = b
                .comment("Derivative gain (rad/s^2 per rad/s of angular velocity). Critical damping is kd = 2*sqrt(kp).")
                .defineInRange("kd", 4.0, 0.0, 1000.0);
        MAX_ANGULAR_ACCELERATION = b
                .comment("Maximum righting angular acceleration in rad/s^2. Mass-independent safety cap; higher = snappier but jerkier corrections.")
                .defineInRange("max_angular_acceleration", 5.0, 0.0, 1000.0);
        b.pop();
        SPEC = b.build();
    }

    private SkyboundAnchorConfig() {}
}
