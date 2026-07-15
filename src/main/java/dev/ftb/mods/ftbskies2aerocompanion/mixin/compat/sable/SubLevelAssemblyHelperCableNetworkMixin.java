package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.sable;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.logging.LogUtils;
import dev.ftb.mods.ftbskies2aerocompanion.compat.integrateddynamics.IntegratedDynamicsNetworkReform;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * IntegratedDynamics networks do not survive Sable relocating cable blocks into a
 * sub-level: the network is persisted as position-keyed save data separate from the
 * block entities, so after the move the saved path elements no longer resolve and the
 * parts are dropped (cards "pop out") while the relocated parts come up network-dead.
 *
 * <p>We cannot mixin ID's cable classes directly — {@code BlockCable},
 * {@code BlockEntityMultipartTicking} and {@code CableHelpers} load during ID's early
 * setup, before this mod's mixin config is prepared, so any mixin targeting them is
 * silently skipped. Sable's {@code SubLevelAssemblyHelper} loads lazily at assembly
 * time and is reliably mixable (Aeronautics et al. already do).
 *
 * <p>Bracket {@code moveBlocks} with {@link CableHelpers#setRemovingCable(boolean)} — ID's
 * own mechanism for relocating cables — so {@code BlockCable.onRemove} skips
 * {@code onCableRemoving} during the real-world removal, suppressing both the part drop
 * and the stale network teardown.
 *
 * <p>The network reform must run <em>after</em> {@code assembleBlocks} finishes, not inside
 * {@code moveBlocks}. {@code moveBlocks} is called partway through {@code assembleBlocks},
 * before the sub-level's mass tracker is read and its rigid body is created. Reforming
 * there issues {@code setBlock} calls (cable connection updates) into the half-assembled
 * sub-level, which corrupts the mass/physics setup that runs immediately after — the body
 * comes up massless, so the ship renders but never simulates (no gravity, the physics wand
 * can't grab it). Injecting at {@code assembleBlocks} RETURN runs the same reform once the
 * body is fully built, fixing the network without disturbing physics.
 */
@Mixin(value = SubLevelAssemblyHelper.class, remap = false)
public abstract class SubLevelAssemblyHelperCableNetworkMixin {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Inject(method = "moveBlocks", at = @At("HEAD"))
    private static void ftbskies2aero$beginCableMove(ServerLevel level, SubLevelAssemblyHelper.AssemblyTransform transform, Iterable<BlockPos> blocks, CallbackInfo ci) {
        CableHelpers.setRemovingCable(true);
    }

    @Inject(method = "moveBlocks", at = @At("RETURN"))
    private static void ftbskies2aero$endCableMove(ServerLevel level, SubLevelAssemblyHelper.AssemblyTransform transform, Iterable<BlockPos> blocks, CallbackInfo ci) {
        CableHelpers.setRemovingCable(false);
    }

    @Inject(method = "assembleBlocks", at = @At("RETURN"))
    private static void ftbskies2aero$reformAfterAssembly(ServerLevel level, BlockPos anchor, Iterable<BlockPos> blocks, BoundingBox3ic bounds,
                                                          CallbackInfoReturnable<ServerSubLevel> cir,
                                                          @Local SubLevelAssemblyHelper.AssemblyTransform transform) {
        try {
            ServerLevel resultingLevel = transform.getLevel();
            List<BlockPos> positions = new ArrayList<>();
            for (BlockPos block : blocks) {
                positions.add(transform.apply(block));
            }
            IntegratedDynamicsNetworkReform.reform(resultingLevel, positions);
        } catch (Throwable t) {
            LOGGER.error("Failed to reform IntegratedDynamics networks after sub-level assembly", t);
        }
    }
}
