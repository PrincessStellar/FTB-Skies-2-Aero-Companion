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
        float pitch
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
        return tag;
    }

    public static ShipBinding read(CompoundTag tag) {
        return new ShipBinding(
                tag.getUUID("ship"),
                ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(tag.getString("dim"))),
                new Vec3(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z")),
                tag.getFloat("yaw"),
                tag.getFloat("pitch")
        );
    }
}
