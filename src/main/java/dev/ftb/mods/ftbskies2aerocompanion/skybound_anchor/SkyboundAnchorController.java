package dev.ftb.mods.ftbskies2aerocompanion.skybound_anchor;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.physics.mass.MassData;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.joml.Matrix3dc;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = FTBSkies2AeroCompanion.MOD_ID)
public final class SkyboundAnchorController {
    private static final Map<ServerSubLevel, SkyboundAnchorController> INSTANCES = new ConcurrentHashMap<>();
    private static final Vector3dc WORLD_UP = new Vector3d(0.0, 1.0, 0.0);
    private static final double NEVER_TICKED = Double.NEGATIVE_INFINITY;

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

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        INSTANCES.clear();
    }

    public void tick(double partialPhysicsTick, RigidBodyHandle body, double dt) {
        if (partialPhysicsTick == lastTickedPartial) {
            return;
        }
        lastTickedPartial = partialPhysicsTick;

        if (!hasActiveAnchor()) {
            return;
        }

        MassData mass = subLevel.getMassTracker();
        if (mass.isInvalid()) {
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
        }

        // torque (N*m) = I * alpha;  torque impulse = torque * dt.
        inertia.transform(restoringImpulseLocal);
        restoringImpulseLocal.mul(dt);

        body.applyTorqueImpulse(restoringImpulseLocal);
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
