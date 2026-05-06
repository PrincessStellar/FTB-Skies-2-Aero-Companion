package dev.ftb.mods.ftbskies2aerocompanion.ship;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public record ShipBinding(
        UUID shipUuid,
        ResourceKey<Level> shipDimension,
        Vec3 localOffset,
        float yaw,
        float pitch,
        Vec3 lastKnownPos
) {
    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("ship", shipUuid);
        tag.putString("dim", shipDimension.location().toString());
        tag.putDouble("x", localOffset.x);
        tag.putDouble("y", localOffset.y);
        tag.putDouble("z", localOffset.z);
        tag.putFloat("yaw", yaw);
        tag.putFloat("pitch", pitch);
        tag.putDouble("lkx", lastKnownPos.x);
        tag.putDouble("lky", lastKnownPos.y);
        tag.putDouble("lkz", lastKnownPos.z);
        return tag;
    }

    public static ShipBinding read(CompoundTag tag) {
        Vec3 lastKnown = tag.contains("lkx")
                ? new Vec3(tag.getDouble("lkx"), tag.getDouble("lky"), tag.getDouble("lkz"))
                : Vec3.ZERO;
        return new ShipBinding(
                tag.getUUID("ship"),
                ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(tag.getString("dim"))),
                new Vec3(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z")),
                tag.getFloat("yaw"),
                tag.getFloat("pitch"),
                lastKnown
        );
    }
}
