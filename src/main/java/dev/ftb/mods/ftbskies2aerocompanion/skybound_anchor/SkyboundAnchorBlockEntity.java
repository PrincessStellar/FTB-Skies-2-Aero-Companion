package dev.ftb.mods.ftbskies2aerocompanion.skybound_anchor;

import com.mojang.logging.LogUtils;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class SkyboundAnchorBlockEntity extends BlockEntity implements BlockEntitySubLevelActor {
    private static final Logger LOG = LogUtils.getLogger();
    private boolean loggedFirstTick = false;

    public SkyboundAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(ModSkyboundAnchorBlocks.SKYBOUND_ANCHOR_BE.get(), pos, state);
    }

    @Override
    public void sable$physicsTick(ServerSubLevel subLevel, RigidBodyHandle body, double dt) {
        if (!loggedFirstTick) {
            loggedFirstTick = true;
            LOG.info("[SkyboundAnchor] physicsTick @{} subLevel={} dt={}", getBlockPos(), subLevel, dt);
        }
        double partial = SubLevelPhysicsSystem.getCurrentlySteppingSystem().getPartialPhysicsTick();
        SkyboundAnchorController.of(subLevel).tick(partial, body, dt);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level == null || level.isClientSide) {
            return;
        }
        SubLevel containing = Sable.HELPER.getContaining(this);
        if (!(containing instanceof ServerSubLevel serverSubLevel)) {
            return;
        }
        for (BlockEntitySubLevelActor actor : serverSubLevel.getPlot().getBlockEntityActors()) {
            if (actor != this && actor instanceof SkyboundAnchorBlockEntity) {
                return;
            }
        }
        SkyboundAnchorController.detach(serverSubLevel);
    }
}
