package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.ftbteambases;

import dev.ftb.mods.ftbteambases.data.bases.BaseInstanceManager;
import dev.ftb.mods.ftbteambases.util.RegionCoords;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = BaseInstanceManager.class, remap = false)
public interface BaseInstanceManagerAccessor {

    @Accessor("storedGenPos")
    Map<ResourceLocation, RegionCoords> ftbskies2aero$storedGenPos();

    @Accessor("storedZoffset")
    Map<ResourceLocation, Integer> ftbskies2aero$storedZoffset();
}
