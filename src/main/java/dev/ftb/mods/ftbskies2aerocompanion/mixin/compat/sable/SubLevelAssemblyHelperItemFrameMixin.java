package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.sable;

import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(value = SubLevelAssemblyHelper.class, remap = false)
public abstract class SubLevelAssemblyHelperItemFrameMixin {

    @Redirect(
            method = "moveOtherStuff",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;",
                    remap = true
            )
    )
    private static List<Entity> ftbskies2aero$flagAssembledHangingEntities(ServerLevel level, Class<Entity> type, AABB box) {
        List<Entity> entities = level.getEntitiesOfClass(type, box);
        for (Entity entity : entities) {
            if (entity instanceof BlockAttachedEntity && !entity.level().isClientSide) {
                entity.getPersistentData().putBoolean("ftbskies2aero:ship_bound", true);
            }
        }
        return entities;
    }
}
