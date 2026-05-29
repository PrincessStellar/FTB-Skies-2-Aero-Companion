package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.sable;

import com.mojang.logging.LogUtils;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.NetworkHelpers;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
 * <p>Bracket the whole move with {@link CableHelpers#setRemovingCable(boolean)} — ID's
 * own mechanism for relocating cables — so {@code BlockCable.onRemove} skips
 * {@code onCableRemoving}, suppressing both the part drop and the stale network teardown.
 * Afterwards recompute each relocated cable's connections and re-init its network so the
 * sub-level cables form a fresh working network with their parts (and cards) intact.
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
        try {
            ServerLevel resultingLevel = transform.getLevel();
            List<BlockPos> cables = new ArrayList<>();
            for (BlockPos block : blocks) {
                BlockPos newPos = transform.apply(block);
                if (CableHelpers.getCable(resultingLevel, newPos, null).isPresent()) {
                    cables.add(newPos);
                }
            }

            for (BlockPos pos : cables) {
                CableHelpers.getCable(resultingLevel, pos, null).ifPresent(cable -> cable.updateConnections());
            }
            for (BlockPos pos : cables) {
                NetworkHelpers.initNetwork(resultingLevel, pos, null);
            }
        } catch (Throwable t) {
            LOGGER.error("Failed to reform IntegratedDynamics networks after sub-level assembly", t);
        } finally {
            CableHelpers.setRemovingCable(false);
        }
    }
}
