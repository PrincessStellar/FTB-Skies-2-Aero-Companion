package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.integrateddynamics;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.cyclops.integrateddynamics.block.BlockSqueezer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = BlockSqueezer.class, remap = false)
public abstract class BlockSqueezerSubLevelMixin {

    @Redirect(method = "updateEntityAfterFallOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getX()D"))
    private double ftbskies2aero$plotX(Entity entity) {
        SubLevel subLevel = Sable.HELPER.getContaining(entity);
        return subLevel == null ? entity.getX() : ftbskies2aero$plotPos(entity, subLevel).x;
    }

    @Redirect(method = "updateEntityAfterFallOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getY()D"))
    private double ftbskies2aero$plotY(Entity entity) {
        SubLevel subLevel = Sable.HELPER.getContaining(entity);
        return subLevel == null ? entity.getY() : ftbskies2aero$plotPos(entity, subLevel).y;
    }

    @Redirect(method = "updateEntityAfterFallOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getZ()D"))
    private double ftbskies2aero$plotZ(Entity entity) {
        SubLevel subLevel = Sable.HELPER.getContaining(entity);
        return subLevel == null ? entity.getZ() : ftbskies2aero$plotPos(entity, subLevel).z;
    }

    private static Vec3 ftbskies2aero$plotPos(Entity entity, SubLevel subLevel) {
        return JOMLConversion.toMojang(subLevel.logicalPose().transformPositionInverse(JOMLConversion.toJOML(entity.position())));
    }
}
