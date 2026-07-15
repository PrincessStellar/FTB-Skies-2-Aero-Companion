package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.sg_economy;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.sirgrantd.sg_economy.config.ServerConfig", remap = false)
public class SGEconomyServerConfigMixin {

    @WrapOperation(
            method = "bakeConfig",
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/network/PacketDistributor;sendToAllPlayers(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;[Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V"),
            require = 0,
            expect = 0
    )
    private static void ftbskies2aero$onlyBroadcastWithServer(CustomPacketPayload payload, CustomPacketPayload[] additional, Operation<Void> original) {
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            original.call(payload, additional);
        }
    }
}
