package dev.ftb.mods.ftbskies2aerocompanion.ship;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import dev.ftb.mods.ftbessentials.util.TeleportPos;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class ShipBindings {
    private ShipBindings() {}

    public static Optional<AbstractContraptionEntity> findShip(Player player) {
        Entity root = player.getRootVehicle();
        if (root != player && root instanceof AbstractContraptionEntity ship) {
            return Optional.of(ship);
        }
        AABB search = player.getBoundingBox().inflate(0.5).expandTowards(0, -2, 0);
        return player.level().getEntitiesOfClass(AbstractContraptionEntity.class, search).stream().findFirst();
    }

    public static ShipBinding capture(AbstractContraptionEntity ship, Vec3 worldPos, float yaw, float pitch) {
        Vec3 local = ship.toLocalVector(worldPos, 0f);
        return new ShipBinding(ship.getUUID(), ship.level().dimension(), local, yaw, pitch);
    }

    public static Optional<TeleportPos> resolve(MinecraftServer server, ShipBinding binding) {
        if (server == null) return Optional.empty();
        ServerLevel level = server.getLevel(binding.shipDimension());
        if (level == null) return Optional.empty();
        Entity entity = level.getEntity(binding.shipUuid());
        if (!(entity instanceof AbstractContraptionEntity ship) || !ship.isAlive()) return Optional.empty();
        Vec3 world = ship.toGlobalVector(binding.localOffset(), 1.0f);
        return Optional.of(new TeleportPos(level.dimension(), BlockPos.containing(world), binding.yaw(), binding.pitch()));
    }

    public static TeleportPos worldSpawnFallback(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return new TeleportPos(overworld.dimension(), overworld.getSharedSpawnPos(), 0f, 0f);
    }
}
