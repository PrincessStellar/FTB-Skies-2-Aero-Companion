package dev.ftb.mods.ftbskies2aerocompanion.skybound_anchor;

import com.mojang.logging.LogUtils;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.physics.mass.MassData;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import org.joml.Matrix3dc;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SkyboundAnchorController {
    private static final Logger LOG = LogUtils.getLogger();
    private static final Map<ServerSubLevel, SkyboundAnchorController> INSTANCES = new ConcurrentHashMap<>();
    private static final Vector3dc WORLD_UP = new Vector3d(0.0, 1.0, 0.0);
    private static final double NEVER_TICKED = Double.NEGATIVE_INFINITY;
    private long lastLogTick = 0L;

    private final ServerSubLevel subLevel;
    private final Vector3d currentUpWorld = new Vector3d();
    private final Vector3d errorAxisWorld = new Vector3d();
    private final Vector3d errorAxisLocal = new Vector3d();
    private final Vector3d angularVelocityWorld = new Vector3d();
    private final Vector3d angularVelocityLocal = new Vector3d();
    private final Vector3d worldUpLocal = new Vector3d();
    private final Vector3d restoringImpulseLocal = new Vector3d();
    private final Vector3d dampingImpulseLocal = new Vector3d();
    private double lastTickedPartial = NEVER_TICKED;

    private SkyboundAnchorController(ServerSubLevel subLevel) {
        this.subLevel = subLevel;
    }

    public static SkyboundAnchorController of(ServerSubLevel subLevel) {
        return INSTANCES.computeIfAbsent(subLevel, SkyboundAnchorController::new);
    }

    static void detach(ServerSubLevel subLevel) {
        INSTANCES.remove(subLevel);
    }

    public void tick(double partialPhysicsTick, RigidBodyHandle body, double dt) {
        if (partialPhysicsTick == lastTickedPartial) {
            return;
        }
        lastTickedPartial = partialPhysicsTick;

        long now = System.currentTimeMillis();
        boolean logThisTick = now - lastLogTick > 2000;
        if (logThisTick) {
            lastLogTick = now;
        }

        if (!hasActiveAnchor()) {
            if (logThisTick) LOG.info("[SkyboundAnchor] tick: no active anchor in subLevel={}", subLevel);
            return;
        }

        MassData mass = subLevel.getMassTracker();
        if (mass.isInvalid()) {
            if (logThisTick) LOG.info("[SkyboundAnchor] tick: mass invalid for subLevel={}", subLevel);
            return;
        }

        Quaterniondc orientation = subLevel.logicalPose().orientation();
        Matrix3dc inertia = mass.getInertiaTensor();

        body.getAngularVelocity(angularVelocityWorld);
        orientation.transformInverse(angularVelocityWorld, angularVelocityLocal);
        orientation.transformInverse(WORLD_UP, worldUpLocal);

        orientation.transform(WORLD_UP, currentUpWorld);
        currentUpWorld.cross(WORLD_UP, errorAxisWorld);
        orientation.transformInverse(errorAxisWorld, errorAxisLocal);

        double kp = SkyboundAnchorConfig.KP.get();
        double kd = SkyboundAnchorConfig.KD.get();
        double maxAlpha = SkyboundAnchorConfig.MAX_ANGULAR_ACCELERATION.get();

        // PD control on angular acceleration (rad/s^2), mass-independent.
        // alpha = kp * errorAxis - kd * angularVelocity, in body frame.
        double ax = kp * errorAxisLocal.x - kd * angularVelocityLocal.x;
        double az = kp * errorAxisLocal.z - kd * angularVelocityLocal.z;

        // Project onto the plane perpendicular to worldUpLocal (no yaw control).
        double ay = 0.0;
        if (Math.abs(worldUpLocal.y) > 0.001) {
            ay = -(ax * worldUpLocal.x + az * worldUpLocal.z) / worldUpLocal.y;
        }
        restoringImpulseLocal.set(ax, ay, az);

        double alphaMag = restoringImpulseLocal.length();
        if (alphaMag > maxAlpha) {
            restoringImpulseLocal.mul(maxAlpha / alphaMag);
            alphaMag = maxAlpha;
        }

        // torque (N*m) = I * alpha;  torque impulse = torque * dt.
        inertia.transform(restoringImpulseLocal);
        restoringImpulseLocal.mul(dt);

        body.applyTorqueImpulse(restoringImpulseLocal);

        if (logThisTick) {
            LOG.info("[SkyboundAnchor] tick: errAxisLocal=({}, {}, {}) angVelLocal=({}, {}, {}) alpha={} torqueImpulse=({}, {}, {})",
                    errorAxisLocal.x, errorAxisLocal.y, errorAxisLocal.z,
                    angularVelocityLocal.x, angularVelocityLocal.y, angularVelocityLocal.z,
                    alphaMag,
                    restoringImpulseLocal.x, restoringImpulseLocal.y, restoringImpulseLocal.z);
        }
    }

    private boolean hasActiveAnchor() {
        for (BlockEntitySubLevelActor actor : subLevel.getPlot().getBlockEntityActors()) {
            if (actor instanceof SkyboundAnchorBlockEntity) {
                return true;
            }
        }
        return false;
    }
}
