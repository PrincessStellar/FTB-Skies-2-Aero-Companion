package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.simulated;

import com.mojang.logging.LogUtils;
import dev.ftb.mods.ftbskies2aerocompanion.ship.ShipBindings;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "dev.simulated_team.simulated.util.SimAssemblyHelper", remap = false)
public abstract class SimAssemblyHelperShipHomeMixin {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Inject(method = "disassembleSubLevel", at = @At("HEAD"))
    private static void ftbskies2aero$snapshotShipHomeOnDisassemble(Level level, SubLevel subLevel, BlockPos worldPos,
                                                                    BlockPos assemblerPos, Rotation rotation, boolean flag,
                                                                    CallbackInfo ci) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        try {
            ShipBindings.onSubLevelDisassembled(serverLevel.getServer(), subLevel);
        } catch (Throwable t) {
            LOGGER.error("Failed to snapshot ship-home bindings before disassembly", t);
        }
    }
}
