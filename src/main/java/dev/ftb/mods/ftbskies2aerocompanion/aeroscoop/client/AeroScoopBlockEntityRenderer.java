package dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.AeroScoopBlock;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.AeroScoopBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class AeroScoopBlockEntityRenderer implements BlockEntityRenderer<AeroScoopBlockEntity> {
    public AeroScoopBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(AeroScoopBlockEntity be, float partialTicks, PoseStack pose, MultiBufferSource buffers,
                       int packedLight, int packedOverlay) {
        ItemStack filter = be.getFilter();
        if (filter.isEmpty()) return;

        BlockState state = be.getBlockState();
        Direction facing = state.hasProperty(AeroScoopBlock.FACING) ? state.getValue(AeroScoopBlock.FACING) : Direction.NORTH;

        pose.pushPose();
        // center of the block
        pose.translate(0.5, 0.5, 0.5);

        // orient the mesh perpendicular to the intake face (the mesh sits inside, facing forward)
        switch (facing) {
            case UP:    pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90));  break;
            case DOWN:  pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90)); break;
            case NORTH:                                                              break;
            case SOUTH: pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180)); break;
            case EAST:  pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90)); break;
            case WEST:  pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));  break;
        }

        // pull the mesh inward from the intake face by ~0.18 of a block
        pose.translate(0.0, 0.0, -0.18);
        // scale the mesh down so it fits inside the casing
        pose.scale(0.6F, 0.6F, 0.6F);

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        itemRenderer.renderStatic(filter, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                pose, buffers, be.getLevel(), 0);

        pose.popPose();
    }
}
