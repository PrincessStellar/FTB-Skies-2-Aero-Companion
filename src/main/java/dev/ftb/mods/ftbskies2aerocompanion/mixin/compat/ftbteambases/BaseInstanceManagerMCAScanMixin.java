package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.ftbteambases;

import dev.ftb.mods.ftbteambases.data.bases.BaseInstanceManager;
import dev.ftb.mods.ftbteambases.util.RegionCoords;
import dev.ftb.mods.ftbteambases.util.RegionExtents;
import dev.ftb.mods.ftblibrary.math.XZ;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = BaseInstanceManager.class, remap = false)
public abstract class BaseInstanceManagerMCAScanMixin {

    @Inject(method = "anyMCAFilesPresent", at = @At("HEAD"), cancellable = true)
    private void ftbskies2aero$occupiedByRecordedBasesOnly(MinecraftServer server, ResourceLocation dimensionId, RegionCoords start, XZ size,
                                                           CallbackInfoReturnable<Boolean> cir) {
        BaseInstanceManager self = (BaseInstanceManager) (Object) this;
        int minX = start.x();
        int minZ = start.z();
        int maxX = minX + Math.max(1, size.x()) - 1;
        int maxZ = minZ + Math.max(1, size.z()) - 1;

        List<RegionExtents> occupied = new ArrayList<>();
        self.allLiveBases().values().forEach(base -> {
            if (base.dimension().location().equals(dimensionId)) {
                occupied.add(base.extents());
            }
        });
        self.getArchivedBases().forEach(base -> occupied.add(base.extents()));

        for (RegionExtents extents : occupied) {
            if (extents.start().x() <= maxX && extents.end().x() >= minX
                    && extents.start().z() <= maxZ && extents.end().z() >= minZ) {
                cir.setReturnValue(true);
                return;
            }
        }
        cir.setReturnValue(false);
    }
}
