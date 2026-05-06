package dev.ftb.mods.ftbskies2aerocompanion.mixin;

import dev.ftb.mods.ftbessentials.util.FTBEPlayerData;
import dev.ftb.mods.ftbessentials.util.SavedTeleportManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SavedTeleportManager.HomeManager.class)
public interface HomeManagerAccessor {
    @Accessor("playerData")
    FTBEPlayerData ftbskies2aero$getPlayerData();
}
