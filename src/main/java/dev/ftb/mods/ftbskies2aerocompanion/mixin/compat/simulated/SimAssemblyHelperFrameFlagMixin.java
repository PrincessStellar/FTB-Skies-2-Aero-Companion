package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.simulated;

import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.simulated_team.simulated.util.SimAssemblyHelper", remap = false)
public abstract class SimAssemblyHelperFrameFlagMixin {

    @Inject(method = "disassembleSubLevel", at = @At("HEAD"))
    private static void ftbskies2aero$flagShipHangingEntities(Level level, SubLevel subLevel, BlockPos worldPos, BlockPos assemblerPos,
                                                              Rotation rotation, boolean flag, CallbackInfo ci) {
        try {
            if (level.isClientSide) {
                return;
            }
            BoundingBox3ic box = subLevel.getPlot().getBoundingBox();
            AABB area = new AABB(box.minX(), box.minY(), box.minZ(), box.maxX() + 1, box.maxY() + 1, box.maxZ() + 1).inflate(2.0);
            for (BlockAttachedEntity entity : level.getEntitiesOfClass(BlockAttachedEntity.class, area)) {
                entity.getPersistentData().putBoolean("ftbskies2aero:ship_bound", true);
            }
        } catch (Throwable ignored) {
        }
    }
}
