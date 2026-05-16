package dev.ftb.mods.ftbskies2aerocompanion.skybound_anchor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SkyboundAnchorBlock extends Block implements EntityBlock {
    public SkyboundAnchorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SkyboundAnchorBlockEntity(pos, state);
    }
}
