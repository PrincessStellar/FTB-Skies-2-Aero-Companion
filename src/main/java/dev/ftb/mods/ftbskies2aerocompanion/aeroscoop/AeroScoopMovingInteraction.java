package dev.ftb.mods.ftbskies2aerocompanion.aeroscoop;

import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.apache.commons.lang3.tuple.MutablePair;

public class AeroScoopMovingInteraction extends MovingInteractionBehaviour {

    @Override
    public boolean handlePlayerInteraction(Player player, InteractionHand hand, BlockPos localPos,
                                           AbstractContraptionEntity entity) {
        if (hand == InteractionHand.OFF_HAND) return false;
        if (player.level().isClientSide) return true;

        Contraption contraption = entity.getContraption();
        MovementContext context = null;
        for (MutablePair<StructureBlockInfo, MovementContext> pair : contraption.getActors()) {
            MovementContext ctx = pair.right;
            if (ctx != null && localPos.equals(ctx.localPos)) {
                context = ctx;
                break;
            }
        }
        if (context == null) return false;

        HolderLookup.Provider registries = player.level().registryAccess();
        CompoundTag beData = context.blockEntityData != null ? context.blockEntityData : new CompoundTag();

        ItemStackHandler filterHandler = new ItemStackHandler(1);
        if (beData.contains("Filter")) {
            filterHandler.deserializeNBT(registries, beData.getCompound("Filter"));
        }

        ItemStack current = filterHandler.getStackInSlot(0);
        ItemStack heldStack = player.getItemInHand(hand);

        boolean changed;
        if (!current.isEmpty()) {
            ItemStack popped = current.copy();
            filterHandler.setStackInSlot(0, ItemStack.EMPTY);
            if (!player.getInventory().add(popped)) {
                player.drop(popped, false);
            }
            changed = true;
        } else if (heldStack.getItem() instanceof MeshItem) {
            filterHandler.setStackInSlot(0, heldStack.copyWithCount(1));
            if (!player.getAbilities().instabuild) {
                heldStack.shrink(1);
            }
            changed = true;
        } else {
            return false;
        }

        if (!changed) return false;

        beData.put("Filter", filterHandler.serializeNBT(registries));
        context.blockEntityData = beData;

        StructureBlockInfo info = contraption.getBlocks().get(localPos);
        if (info != null) {
            CompoundTag updated = info.nbt() != null ? info.nbt().copy() : new CompoundTag();
            updated.put("Filter", filterHandler.serializeNBT(registries));
            entity.setBlock(localPos, new StructureBlockInfo(info.pos(), info.state(), updated));
        }

        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.6F, 1.0F);
        return true;
    }
}
