package dev.ftb.mods.ftbskies2aerocompanion.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Entity.class)
public abstract class EntityCollisionGuardMixin {

    @Unique
    private static final Logger ftbskies2aero$LOG = LoggerFactory.getLogger("CollisionGuard");

    @Unique
    private static final double ftbskies2aero$MAX_COLLISION_SPAN = 512.0;

    @Unique
    private static long ftbskies2aero$lastWarnMs = 0L;

    @Inject(method = "collectColliders", at = @At("HEAD"), cancellable = true)
    private static void ftbskies2aero$guardCollisionSweep(Entity entity, Level level, List<VoxelShape> collisions,
                                                          AABB box, CallbackInfoReturnable<List<VoxelShape>> cir) {
        double xs = box.getXsize();
        double ys = box.getYsize();
        double zs = box.getZsize();
        boolean degenerate = !Double.isFinite(xs) || !Double.isFinite(ys) || !Double.isFinite(zs)
                || xs > ftbskies2aero$MAX_COLLISION_SPAN
                || ys > ftbskies2aero$MAX_COLLISION_SPAN
                || zs > ftbskies2aero$MAX_COLLISION_SPAN;
        if (!degenerate) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - ftbskies2aero$lastWarnMs > 5000L) {
            ftbskies2aero$lastWarnMs = now;
            ftbskies2aero$LOG.warn("Skipped an oversized block-collision sweep ({}x{}x{}) for {} at {} — likely a Sable sub-level collision pathology; block collisions skipped for this move to keep the tick alive",
                    xs, ys, zs,
                    entity == null ? "unknown entity" : entity.getType(),
                    entity == null ? "?" : entity.position());
        }
        cir.setReturnValue(collisions);
    }
}
