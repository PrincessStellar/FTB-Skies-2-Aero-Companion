package dev.ftb.mods.ftbskies2aerocompanion.voidsea;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.joml.Vector3d;

import java.util.List;

@EventBusSubscriber(modid = FTBSkies2AeroCompanion.MOD_ID)
public final class VoidSeaFloorHandler {

    private VoidSeaFloorHandler() {}

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!VoidSeaConfig.ENABLED.get()) return;

        double seaLevel = VoidSeaConfig.SEA_LEVEL.get();
        MinecraftServer server = event.getServer();

        for (ServerLevel level : server.getAllLevels()) {
            ServerSubLevelContainer container;
            try {
                container = SubLevelContainer.getContainer(level);
            } catch (Throwable t) {
                continue;
            }
            if (container == null) continue;

            List<? extends SubLevel> subs = container.getAllSubLevels();
            for (int i = 0; i < subs.size(); i++) {
                SubLevel sub = subs.get(i);
                if (!(sub instanceof ServerSubLevel serverSub) || sub.isRemoved()) continue;
                try {
                    clampToSea(serverSub, seaLevel);
                } catch (Throwable t) {
                    // a single sub-level must never break the sweep for the rest
                }
            }
        }
    }

    private static void clampToSea(ServerSubLevel sub, double seaLevel) {
        Pose3dc pose = sub.logicalPose();
        Vec3 origin = pose.transformPosition(Vec3.ZERO);
        if (origin.y >= seaLevel) return;

        RigidBodyHandle handle = RigidBodyHandle.of(sub);
        if (handle == null || !handle.isValid()) return;

        Vector3d velocity = handle.getLinearVelocity(new Vector3d());
        if (velocity.y < 0.0) {
            handle.addLinearAndAngularVelocity(new Vector3d(0.0, -velocity.y, 0.0), new Vector3d());
        }
        handle.teleport(new Vector3d(origin.x, seaLevel, origin.z), pose.orientation());
    }
}
