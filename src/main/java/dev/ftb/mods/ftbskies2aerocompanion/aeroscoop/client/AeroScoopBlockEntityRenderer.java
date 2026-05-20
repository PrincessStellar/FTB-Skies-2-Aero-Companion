package dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
        pose.translate(0.5, 0.5, 0.5);

        switch (facing) {
            case UP:    pose.mulPose(Axis.XP.rotationDegrees(90));  break;
            case DOWN:  pose.mulPose(Axis.XP.rotationDegrees(-90)); break;
            case NORTH:                                             break;
            case SOUTH: pose.mulPose(Axis.YP.rotationDegrees(180)); break;
            case EAST:  pose.mulPose(Axis.YP.rotationDegrees(-90)); break;
            case WEST:  pose.mulPose(Axis.YP.rotationDegrees(90));  break;
        }

        pose.translate(0.0, 0.0, -0.18);
        pose.scale(0.6F, 0.6F, 0.6F);

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        itemRenderer.renderStatic(filter, ItemDisplayContext.FIXED, packedLight, packedOverlay,
                pose, buffers, be.getLevel(), 0);

        pose.popPose();
    }
}
