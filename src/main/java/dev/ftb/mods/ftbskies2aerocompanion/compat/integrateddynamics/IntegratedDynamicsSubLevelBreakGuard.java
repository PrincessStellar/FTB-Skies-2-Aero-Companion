package dev.ftb.mods.ftbskies2aerocompanion.compat.integrateddynamics;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import dev.ryanhcode.sable.Sable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = FTBSkies2AeroCompanion.MOD_ID)
public final class IntegratedDynamicsSubLevelBreakGuard {

    private static final Logger LOGGER = LoggerFactory.getLogger("IDSubLevelBreak");

    private static Boolean idLoaded;

    private IntegratedDynamicsSubLevelBreakGuard() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (idLoaded == null) {
            idLoaded = ModList.get().isLoaded("integrateddynamics");
        }
        if (!idLoaded) {
            return;
        }
        LevelAccessor levelAccessor = event.getLevel();
        if (!(levelAccessor instanceof ServerLevel level)) {
            return;
        }
        BlockPos pos = event.getPos();
        if (Sable.HELPER.getContaining(level, pos) == null) {
            return;
        }
        if (CableHelpers.getCable(level, pos, null).isEmpty()) {
            return;
        }

        event.setCanceled(true);
        Player player = event.getPlayer();
        try {
            CableHelpers.removeCable(level, pos, player);
        } catch (Throwable t) {
            LOGGER.warn("IntegratedDynamics cable removal threw on a Sable sub-level at {}; the parts were already dropped, forcing safe block removal", pos, t);
            CableHelpers.setRemovingCable(true);
            try {
                level.destroyBlock(pos, true, player);
            } finally {
                CableHelpers.setRemovingCable(false);
            }
        }

        try {
            List<BlockPos> around = new ArrayList<>(7);
            around.add(pos.immutable());
            for (Direction direction : Direction.values()) {
                around.add(pos.relative(direction).immutable());
            }
            IntegratedDynamicsNetworkReform.reform(level, around);
        } catch (Throwable t) {
            LOGGER.warn("Failed to reform IntegratedDynamics network after a sub-level cable break at {}", pos, t);
        }
    }
}
