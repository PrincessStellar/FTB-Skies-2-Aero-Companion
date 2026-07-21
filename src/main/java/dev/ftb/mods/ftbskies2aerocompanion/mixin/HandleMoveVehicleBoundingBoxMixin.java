package dev.ftb.mods.ftbskies2aerocompanion.mixin;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * A ridden flying Happy Ghast (VanillaBackport) comes through with an {@code AABB.INFINITE}
 * bounding box. {@code ServerGamePacketListenerImpl.handleMoveVehicle} validates the vehicle's new
 * position with {@code serverLevel.noCollision(vehicle, vehicle.getBoundingBox())} every movement
 * packet, so it queries entities with that near-infinite box; Sable aborts the oversized query and
 * error-logs it (with a stack trace) each time, flooding the server log by tens of MB a minute.
 *
 * <p>Redirect the vehicle's {@code getBoundingBox()} in that check: if the box is non-finite or
 * absurdly large, substitute a small finite box around the vehicle's position so the collision
 * query stays sane. Normal boxes pass through untouched. This stops the infinite query at its
 * source; the throttle on Sable's log ({@code SubLevelEntityGetterLogSpamMixin}) remains as a
 * backstop for any other origin. The ghast's invalid box itself is an upstream bug.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class HandleMoveVehicleBoundingBoxMixin {

    @Redirect(
            method = "handleMoveVehicle",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getBoundingBox()Lnet/minecraft/world/phys/AABB;")
    )
    private AABB ftbskies2aero$sanitizeVehicleBox(Entity vehicle) {
        AABB box = vehicle.getBoundingBox();
        if (ftbskies2aero$isFinite(box) && box.getSize() <= 100000.0) {
            return box;
        }
        Vec3 pos = vehicle.position();
        double x = ftbskies2aero$clamp(pos.x);
        double y = ftbskies2aero$clamp(pos.y);
        double z = ftbskies2aero$clamp(pos.z);
        return new AABB(x - 2.0, y - 2.0, z - 2.0, x + 2.0, y + 2.0, z + 2.0);
    }

    @Unique
    private static boolean ftbskies2aero$isFinite(AABB b) {
        return Double.isFinite(b.minX) && Double.isFinite(b.minY) && Double.isFinite(b.minZ)
                && Double.isFinite(b.maxX) && Double.isFinite(b.maxY) && Double.isFinite(b.maxZ);
    }

    @Unique
    private static double ftbskies2aero$clamp(double v) {
        if (!Double.isFinite(v)) {
            return 0.0;
        }
        return Math.max(-3.0E7, Math.min(3.0E7, v));
    }
}
