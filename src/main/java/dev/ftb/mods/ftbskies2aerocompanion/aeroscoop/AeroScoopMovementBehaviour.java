package dev.ftb.mods.ftbskies2aerocompanion.aeroscoop;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.recipe.AeroScoopRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.List;

public class AeroScoopMovementBehaviour implements MovementBehaviour {
    private static final String TICK_KEY = "AeroScoopTick";

    @Override
    public boolean isActive(MovementContext context) {
        return !context.world.isClientSide;
    }

    @Override
    public void tick(MovementContext context) {
        if (context.world.isClientSide) return;
        if (context.disabled) return;

        Level level = context.world;
        HolderLookup.Provider registries = level.registryAccess();
        CompoundTag beData = context.blockEntityData != null ? context.blockEntityData : new CompoundTag();

        ItemStackHandler filterHandler = new ItemStackHandler(1);
        ItemStackHandler outputHandler = new ItemStackHandler(AeroScoopBlockEntity.OUTPUT_SLOTS);
        if (beData.contains("Filter")) {
            filterHandler.deserializeNBT(registries, beData.getCompound("Filter"));
        }
        if (beData.contains("Output")) {
            outputHandler.deserializeNBT(registries, beData.getCompound("Output"));
        }

        boolean dirty = pushToContraption(context, outputHandler);

        int tick = context.data.getInt(TICK_KEY) + 1;
        if (tick < 20) {
            context.data.putInt(TICK_KEY, tick);
            if (dirty) {
                beData.put("Output", outputHandler.serializeNBT(registries));
                context.blockEntityData = beData;
            }
            return;
        }
        context.data.putInt(TICK_KEY, 0);

        ItemStack filter = filterHandler.getStackInSlot(0);
        if (filter.isEmpty()) {
            if (dirty) {
                beData.put("Output", outputHandler.serializeNBT(registries));
                context.blockEntityData = beData;
            }
            return;
        }

        Vec3 worldPos = context.position;
        if (worldPos == null) return;
        BlockPos pos = BlockPos.containing(worldPos);

        Direction intake = rotatedFacing(context);
        if (!AeroScoopTickLogic.isIntakeClear(level, pos, intake)) {
            if (dirty) {
                beData.put("Output", outputHandler.serializeNBT(registries));
                context.blockEntityData = beData;
            }
            return;
        }

        AeroScoopRecipe recipe = AeroScoopTickLogic.pickRecipe(level, pos, filter, level.random);
        if (recipe == null) {
            if (dirty) {
                beData.put("Output", outputHandler.serializeNBT(registries));
                context.blockEntityData = beData;
            }
            return;
        }

        MeshTier tier = MeshTier.of(filter);
        if (tier == null) return;

        if (level.random.nextFloat() >= tier.efficiency) {
            if (dirty) {
                beData.put("Output", outputHandler.serializeNBT(registries));
                context.blockEntityData = beData;
            }
            return;
        }

        List<ItemStack> rolled = AeroScoopTickLogic.rollResults(recipe, level.random);
        List<ItemStack> overflow = AeroScoopTickLogic.insertAll(outputHandler, rolled);
        for (ItemStack stack : overflow) {
            // Drop overflow at the world position of the moving scoop.
            Containers.dropItemStack(level, worldPos.x, worldPos.y, worldPos.z, stack);
        }

        if (!tier.unbreakable() && filter.isDamageableItem()) {
            int cost = AeroScoopTickLogic.durabilityCostFor(level.dimension());
            int newDamage = filter.getDamageValue() + cost;
            if (newDamage >= filter.getMaxDamage()) {
                filterHandler.setStackInSlot(0, ItemStack.EMPTY);
                level.playSound(null, BlockPos.containing(worldPos),
                        SoundEvents.ITEM_BREAK,
                        SoundSource.BLOCKS,
                        0.8F, 0.8F + level.random.nextFloat() * 0.4F);
            } else {
                filter.setDamageValue(newDamage);
            }
        }

        // Push again after producing so freshly-rolled items can flow this tick instead of sitting one cycle.
        pushToContraption(context, outputHandler);

        beData.put("Filter", filterHandler.serializeNBT(registries));
        beData.put("Output", outputHandler.serializeNBT(registries));
        context.blockEntityData = beData;
    }

    private static boolean pushToContraption(MovementContext context, ItemStackHandler outputHandler) {
        if (context.contraption == null) return false;
        IItemHandler target = context.contraption.getStorage().getAllItems();
        if (target == null) return false;
        boolean moved = false;
        for (int slot = 0; slot < outputHandler.getSlots(); slot++) {
            ItemStack stack = outputHandler.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            ItemStack leftover = ItemHandlerHelper.insertItemStacked(target, stack.copy(), false);
            int delta = stack.getCount() - leftover.getCount();
            if (delta > 0) {
                outputHandler.extractItem(slot, delta, false);
                moved = true;
            }
        }
        return moved;
    }

    @Override
    public void stopMoving(MovementContext context) {
        // Disassembly: the BE will be restored from context.blockEntityData by Create. Nothing to do.
    }

    private static Direction rotatedFacing(MovementContext context) {
        BlockState state = context.state;
        Direction base = state.hasProperty(AeroScoopBlock.FACING) ? state.getValue(AeroScoopBlock.FACING) : Direction.NORTH;
        if (context.rotation == null) return base;
        Vec3 normal = Vec3.atLowerCornerOf(base.getNormal());
        Vec3 rotated = context.rotation.apply(normal);
        return Direction.getNearest(rotated.x, rotated.y, rotated.z);
    }
}
