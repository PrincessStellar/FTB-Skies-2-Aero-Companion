package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.integrateddynamics;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.cyclops.integrateddynamics.core.block.BlockRayTraceResultComponent;
import org.cyclops.integrateddynamics.core.block.VoxelShapeComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * IntegratedDynamics resolves which cable part was clicked in
 * {@link VoxelShapeComponents#rayTrace(BlockPos, Entity)} by building a ray from the
 * player's eye position and look vector and clipping it against the part shapes at the
 * block position. On a Sable sub-level the block lives at the plot's offset coordinates
 * while the player's eye/look are in the main-level frame, so the ray never crosses the
 * block and no part resolves — the writer/reader GUI never opens even though the part is
 * live server-side.
 *
 * <p>Project the ray endpoints into the sub-level frame via the sub-level's logical pose
 * (the same transform Sable applies in its {@code BlockGetter.clip} overwrite) before
 * clipping. Only kicks in when the position is inside a sub-level; the main level keeps
 * vanilla behavior.
 */
@Mixin(value = VoxelShapeComponents.class, remap = false)
public abstract class VoxelShapeComponentsSubLevelMixin {

    @Shadow
    public abstract BlockRayTraceResultComponent clip(Vec3 startVec, Vec3 endVec, BlockPos pos);

    @Inject(method = "rayTrace", at = @At("HEAD"), cancellable = true, require = 0, expect = 0)
    private void ftbskies2aero$subLevelRayTrace(BlockPos pos, Entity entity, CallbackInfoReturnable<BlockRayTraceResultComponent> cir) {
        if (entity == null) {
            return;
        }
        Level level = entity.level();
        SubLevel subLevel = Sable.HELPER.getContaining(level, pos);
        if (subLevel == null) {
            return;
        }

        double reach = 5.0D;
        if (entity instanceof LivingEntity living) {
            AttributeInstance attr = living.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
            if (attr != null) {
                reach = attr.getValue();
            }
        }

        Vec3 look = entity.getLookAngle();
        Vec3 origin = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
        Vec3 end = origin.add(look.x * reach, look.y * reach, look.z * reach);

        Pose3dc pose = subLevel.logicalPose();
        Vec3 from = JOMLConversion.toMojang(pose.transformPositionInverse(JOMLConversion.toJOML(origin)));
        Vec3 to = JOMLConversion.toMojang(pose.transformPositionInverse(JOMLConversion.toJOML(end)));

        cir.setReturnValue(this.clip(from, to, pos));
    }
}
