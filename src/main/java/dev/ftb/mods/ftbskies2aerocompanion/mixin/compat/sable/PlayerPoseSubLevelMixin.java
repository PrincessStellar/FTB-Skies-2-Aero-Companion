package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.sable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ryanhcode.sable.Sable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * A composter (or any non-full-cube block) on an assembled airship visually breaks crouch: the
 * player sticks in a crouch or cannot crouch. Sable's {@code player_standup} mixin wraps the
 * {@code Level.noCollision} call in {@code Player.canPlayerFitWithinBlocksAndEntitiesWhen} to also
 * test the ship's sub-level blocks, so {@code updatePlayerPose} makes players crouch under low ship
 * ceilings. Its sub-level test ({@code CanFallAtleastHelper.canFallAtleastWithSubLevels}) mis-reads
 * concave shapes like the composter, and client and server land on different poses, so the visual
 * pose desyncs and sticks.
 *
 * <p>Wrap the same call at a higher priority so this runs outermost, and while the player is on a
 * sub-level resolve the pose fit against the parent level only (a fresh, unwrapped
 * {@code noCollision}), which client and server compute identically. Off-ship behaviour is
 * unchanged. The trade-off is that pose no longer auto-lowers under a ship's own low ceilings;
 * movement collision is handled separately and is unaffected.
 */
@Mixin(value = Player.class, priority = 1500)
public abstract class PlayerPoseSubLevelMixin {

    @WrapOperation(
            method = "canPlayerFitWithinBlocksAndEntitiesWhen",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;noCollision(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Z")
    )
    private boolean ftbskies2aero$poseIgnoreSubLevelBlocks(Level level, Entity entity, AABB box, Operation<Boolean> original) {
        try {
            boolean onSubLevel = level.isClientSide
                    ? Sable.HELPER.getContainingClient(entity) != null
                    : Sable.HELPER.getContaining(entity) != null;
            if (onSubLevel) {
                return level.noCollision(entity, box);
            }
        } catch (Throwable ignored) {
        }
        return original.call(level, entity, box);
    }
}
