package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.sable;

import dev.ryanhcode.sable.util.SubLevelInclusiveLevelEntityGetter;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Sable aborts entity lookups whose query AABB is absurdly large (getSize > 100000) and logs an
 * ERROR with a stack trace on every occurrence. VanillaBackport's flying Happy Ghast issues a
 * near-infinite ({@code AABB.INFINITE}) entity query on essentially every movement tick while
 * ridden, so that log grows tens of megabytes per minute and drags the server. The abort itself is
 * correct and cheap; only the per-call logging is harmful.
 *
 * <p>Throttle {@code logError} to at most one line per minute so the condition stays visible in the
 * log without flooding it. This is a stopgap while the root cause (the ghast's invalid AABB) is
 * fixed upstream.
 */
@Mixin(value = SubLevelInclusiveLevelEntityGetter.class, remap = false)
public class SubLevelEntityGetterLogSpamMixin {

    @Unique
    private static final long ftbskies2aero$LOG_INTERVAL_MS = 60_000L;

    @Unique
    private static long ftbskies2aero$lastAbortLog = Long.MIN_VALUE;

    @Inject(method = "logError", at = @At("HEAD"), cancellable = true, remap = false)
    private static void ftbskies2aero$throttleAbortSpam(AABB aabb, CallbackInfo ci) {
        long now = System.currentTimeMillis();
        if (now - ftbskies2aero$lastAbortLog < ftbskies2aero$LOG_INTERVAL_MS) {
            ci.cancel();
        } else {
            ftbskies2aero$lastAbortLog = now;
        }
    }
}
