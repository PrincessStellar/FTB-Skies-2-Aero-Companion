package dev.ftb.mods.ftbskies2aerocompanion.mixin;

import dev.ftb.mods.ftbessentials.api.TeleportResult;
import dev.ftb.mods.ftbessentials.util.TeleportPos;
import dev.ftb.mods.ftbskies2aerocompanion.ship.ShipBinding;
import dev.ftb.mods.ftbskies2aerocompanion.ship.ShipBindings;
import dev.ftb.mods.ftbskies2aerocompanion.ship.ShipBoundTeleport;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Carries a {@link ShipBinding} on a teleport destination so it lands at the live ship
 * pose rather than a stale, block-floored snapshot. {@code TeleportPos} stores an integer
 * {@code BlockPos} and places at {@code (x+0.5, y, z+0.5)}; on a sub-level at fractional
 * offset/rotation that drifts the player up to ~1 block (downward on Y, through the
 * floor). When a binding is present the virtual {@code teleport} resolves the current ship
 * position and places precisely.
 *
 * <p>This is the thin mixin retained until FTBE's {@code TeleportImmediateEvent} is made
 * result-returning (it currently uses Architectury {@code createLoop}, which discards the
 * listener's returned {@code Outcome}, so a listener there cannot move the destination).
 * Saved homes/warps now divert through {@code SavedTeleportEvent.PRE_TELEPORT} instead; this
 * mixin only covers the {@code TeleportPos(Entity)} path — {@code /tpa}, {@code /tpahere},
 * {@code /tpaccept} and {@code /back}, which target a live entity standing on a ship.
 */
@Mixin(TeleportPos.class)
public abstract class TeleportPosShipMixin implements ShipBoundTeleport {

    @Unique
    private ShipBinding ftbskies2aero$binding;

    @Override
    public void ftbskies2aero$setBinding(ShipBinding binding) {
        this.ftbskies2aero$binding = binding;
    }

    @Override
    public ShipBinding ftbskies2aero$getBinding() {
        return this.ftbskies2aero$binding;
    }

    @Inject(method = "<init>(Lnet/minecraft/world/entity/Entity;)V", at = @At("TAIL"))
    private void ftbskies2aero$captureFromEntity(Entity entity, CallbackInfo ci) {
        if (entity instanceof ServerPlayer player) {
            ShipBindings.captureForPlayer(player, player.position(), player.getYRot(), player.getXRot())
                    .ifPresent(binding -> this.ftbskies2aero$binding = binding);
        }
    }

    @Inject(
            method = "teleport(Lnet/minecraft/server/level/ServerPlayer;)Ldev/ftb/mods/ftbessentials/api/TeleportResult;",
            at = @At("HEAD"), cancellable = true)
    private void ftbskies2aero$preciseShipTeleport(ServerPlayer player, CallbackInfoReturnable<TeleportResult> cir) {
        ShipBinding binding = this.ftbskies2aero$binding;
        if (binding == null) {
            return;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        Optional<ShipBindings.Resolved> resolved = ShipBindings.resolveAnchor(server, binding);
        if (resolved.isEmpty()) {
            return;
        }
        ServerLevel level = server.getLevel(binding.shipDimension());
        if (level == null) {
            return;
        }
        ShipBindings.Resolved r = resolved.get();
        Vec3 pos = r.worldPos();
        int experienceLevel = player.experienceLevel;
        player.teleportTo(level, pos.x, pos.y, pos.z, r.yaw(), r.pitch());
        player.setExperienceLevels(experienceLevel);
        cir.setReturnValue(TeleportResult.SUCCESS);
    }
}
