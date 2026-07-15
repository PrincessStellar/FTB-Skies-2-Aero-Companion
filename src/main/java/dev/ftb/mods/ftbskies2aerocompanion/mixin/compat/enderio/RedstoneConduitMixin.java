package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.enderio;

import com.enderio.enderio.api.conduits.bundle.ConduitBundle;
import com.enderio.enderio.api.conduits.network.node.ConduitNode;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * {@code RedstoneConduit.getExtraWorldData} dereferences the conduit's network node
 * ({@code node.getNetwork()}) without null-checking the node itself — it only guards the
 * returned network. A conduit relocated into a Sable sub-level has a null node until its
 * network forms, and Sable serializes the bundle's {@code getUpdateTag} (which calls this)
 * the moment it syncs the sub-level chunk to a tracking player, so the null node crashes
 * the server tick. Return the empty data tag when the node is absent, matching what the
 * method already does when the network is absent.
 */
@Pseudo
@Mixin(value = com.enderio.enderio.content.conduits.type.redstone.RedstoneConduit.class, remap = false)
public abstract class RedstoneConduitMixin {

    @Inject(method = "getExtraWorldData", at = @At("HEAD"), cancellable = true, require = 0, expect = 0)
    private void ftbskies2aero$guardNullNode(ConduitBundle bundle, ConduitNode node, CallbackInfoReturnable<CompoundTag> cir) {
        if (node == null) {
            cir.setReturnValue(new CompoundTag());
        }
    }
}
