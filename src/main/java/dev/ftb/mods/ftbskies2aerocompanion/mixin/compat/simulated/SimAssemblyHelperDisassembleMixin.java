package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.simulated;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.logging.LogUtils;
import dev.ftb.mods.ftbskies2aerocompanion.compat.integrateddynamics.IntegratedDynamicsNetworkReform;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The disassembly counterpart to {@code SubLevelAssemblyHelperCableNetworkMixin}. Assembly
 * (parent to sub-level) is bracketed and reformed; disassembly (sub-level back to parent)
 * runs through {@code SimAssemblyHelper.disassembleSubLevel} and was left uncovered, so the
 * sub-level cables tore their network down / dropped parts on the way out and the cables
 * placed back into the parent came up network-dead. After a disassemble/reassemble cycle the
 * parts (e.g. a steering block reader) lose their bindings.
 *
 * <p>Bracket the whole disassembly with {@link CableHelpers#setRemovingCable(boolean)} so the
 * sub-level cable removal skips ID's teardown, then reform the network in the parent over the
 * region the ship lands in (the sub-level plot bounds mapped through the same
 * {@code AssemblyTransform} the disassembly builds).
 */
@Mixin(targets = "dev.simulated_team.simulated.util.SimAssemblyHelper", remap = false)
public abstract class SimAssemblyHelperDisassembleMixin {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static int[] ftbskies2aero$subLevelBounds;

    @Inject(method = "disassembleSubLevel", at = @At("HEAD"))
    private static void ftbskies2aero$beginDisassembleCableMove(Level level, SubLevel subLevel, BlockPos worldPos, BlockPos assemblerPos,
                                                                Rotation rotation, boolean flag, CallbackInfo ci) {
        CableHelpers.setRemovingCable(true);
        ftbskies2aero$subLevelBounds = null;
        try {
            BoundingBox3ic box = subLevel.getPlot().getBoundingBox();
            ftbskies2aero$subLevelBounds = new int[]{box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ()};
        } catch (Throwable t) {
            LOGGER.error("Failed to read sub-level bounds before disassembly", t);
        }
    }

    @Inject(method = "disassembleSubLevel", at = @At("RETURN"))
    private static void ftbskies2aero$endDisassembleCableMove(Level level, SubLevel subLevel, BlockPos worldPos, BlockPos assemblerPos,
                                                              Rotation rotation, boolean flag, CallbackInfo ci,
                                                              @Local SubLevelAssemblyHelper.AssemblyTransform transform) {
        CableHelpers.setRemovingCable(false);
        int[] bounds = ftbskies2aero$subLevelBounds;
        ftbskies2aero$subLevelBounds = null;
        if (bounds == null) {
            return;
        }
        try {
            ServerLevel resultingLevel = transform.getLevel();
            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
            for (int xi = 0; xi < 2; xi++) {
                for (int yi = 0; yi < 2; yi++) {
                    for (int zi = 0; zi < 2; zi++) {
                        BlockPos corner = new BlockPos(bounds[xi == 0 ? 0 : 3], bounds[yi == 0 ? 1 : 4], bounds[zi == 0 ? 2 : 5]);
                        BlockPos world = transform.apply(corner);
                        minX = Math.min(minX, world.getX());
                        minY = Math.min(minY, world.getY());
                        minZ = Math.min(minZ, world.getZ());
                        maxX = Math.max(maxX, world.getX());
                        maxY = Math.max(maxY, world.getY());
                        maxZ = Math.max(maxZ, world.getZ());
                    }
                }
            }
            IntegratedDynamicsNetworkReform.reform(resultingLevel, new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ));
        } catch (Throwable t) {
            LOGGER.error("Failed to reform IntegratedDynamics networks after sub-level disassembly", t);
        }
    }
}
