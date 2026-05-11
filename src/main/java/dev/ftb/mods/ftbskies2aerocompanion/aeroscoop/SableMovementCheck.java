package dev.ftb.mods.ftbskies2aerocompanion.aeroscoop;

import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;
import org.joml.Vector3dc;

public final class SableMovementCheck {
    private static final double MOVEMENT_EPSILON_SQ = 1.0e-6;
    private static final boolean SABLE_LOADED = ModList.get().isLoaded("sable");

    private SableMovementCheck() {}

    public static boolean isOnMovingShip(BlockEntity be) {
        if (!SABLE_LOADED) return false;
        try {
            SubLevelAccess sub = SableCompanion.INSTANCE.getContaining(be);
            if (sub == null) return false;
            Pose3dc cur = sub.logicalPose();
            Pose3dc prev = sub.lastPose();
            if (cur == null || prev == null) return false;
            Vector3dc a = cur.position();
            Vector3dc b = prev.position();
            double dx = a.x() - b.x();
            double dy = a.y() - b.y();
            double dz = a.z() - b.z();
            return (dx * dx + dy * dy + dz * dz) > MOVEMENT_EPSILON_SQ;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
