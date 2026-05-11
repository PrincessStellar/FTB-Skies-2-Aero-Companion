package dev.ftb.mods.ftbskies2aerocompanion.aeroscoop;

import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.recipe.AeroScoopRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

import java.util.List;

public class AeroScoopBlockEntity extends BlockEntity {
    public static final int OUTPUT_SLOTS = 9;

    public final ItemStackHandler filterHandler = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() instanceof MeshItem;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            syncToClients();
        }
    };

    public final ItemStackHandler outputHandler = new ItemStackHandler(OUTPUT_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final OutputOnlyWrapper externalView = new OutputOnlyWrapper(outputHandler);

    private int tickCounter;

    public AeroScoopBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AIR_FILTER_BE.get(), pos, state);
    }

    public ItemStack getFilter() {
        return filterHandler.getStackInSlot(0);
    }

    public Direction getIntakeFacing() {
        BlockState state = getBlockState();
        DirectionProperty prop = AeroScoopBlock.FACING;
        return state.hasProperty(prop) ? state.getValue(prop) : Direction.NORTH;
    }

    public IItemHandler getOutputHandlerExternal() {
        return externalView;
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        tickCounter++;
        if (tickCounter < 20) {
            // try to push outputs to neighbours every tick
            pushOutputs(level, pos, state);
            return;
        }
        tickCounter = 0;

        ItemStack filter = getFilter();
        if (filter.isEmpty()) return;
        Direction intake = getIntakeFacing();
        if (!AeroScoopTickLogic.isIntakeClear(level, pos, intake)) return;

        // Stationary placement does not scoop — air must be moving past the intake.
        // Create contraptions use AeroScoopMovementBehaviour; here we cover Sable sub-levels (ships).
        if (!SableMovementCheck.isOnMovingShip(this)) {
            return;
        }

        AeroScoopRecipe recipe = AeroScoopTickLogic.pickRecipe(level, pos, filter, level.random);
        if (recipe == null) return;

        MeshTier tier = MeshTier.of(filter);
        if (tier == null) return;

        if (level.random.nextFloat() >= tier.efficiency) return;

        List<ItemStack> rolled = AeroScoopTickLogic.rollResults(recipe, level.random);
        List<ItemStack> overflow = AeroScoopTickLogic.insertAll(outputHandler, rolled);
        for (ItemStack stack : overflow) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
        }

        if (!tier.unbreakable() && filter.isDamageableItem()) {
            int cost = AeroScoopTickLogic.durabilityCostFor(level.dimension());
            int newDamage = filter.getDamageValue() + cost;
            if (newDamage >= filter.getMaxDamage()) {
                filterHandler.setStackInSlot(0, ItemStack.EMPTY);
                level.playSound(null, pos,
                        net.minecraft.sounds.SoundEvents.ITEM_BREAK,
                        net.minecraft.sounds.SoundSource.BLOCKS,
                        0.8F, 0.8F + level.random.nextFloat() * 0.4F);
            } else {
                filter.setDamageValue(newDamage);
                syncToClients();
            }
        }
        pushOutputs(level, pos, state);
        setChanged();
    }

    private void pushOutputs(Level level, BlockPos pos, BlockState state) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbour = pos.relative(dir);
            IItemHandler target = level.getCapability(Capabilities.ItemHandler.BLOCK, neighbour, dir.getOpposite());
            if (target == null) continue;
            for (int slot = 0; slot < outputHandler.getSlots(); slot++) {
                ItemStack stack = outputHandler.getStackInSlot(slot);
                if (stack.isEmpty()) continue;
                ItemStack toMove = stack.copy();
                ItemStack leftover = net.neoforged.neoforge.items.ItemHandlerHelper.insertItemStacked(target, toMove, false);
                int moved = stack.getCount() - leftover.getCount();
                if (moved > 0) {
                    outputHandler.extractItem(slot, moved, false);
                }
            }
        }
    }

    public void dropContents(Level level, BlockPos pos) {
        NonNullList<ItemStack> drops = NonNullList.create();
        for (int i = 0; i < filterHandler.getSlots(); i++) {
            ItemStack s = filterHandler.getStackInSlot(i);
            if (!s.isEmpty()) drops.add(s);
        }
        for (int i = 0; i < outputHandler.getSlots(); i++) {
            ItemStack s = outputHandler.getStackInSlot(i);
            if (!s.isEmpty()) drops.add(s);
        }
        Containers.dropContents(level, pos, drops);
    }

    public boolean tryInsertFilter(Player player, ItemStack heldStack) {
        if (!getFilter().isEmpty()) {
            ItemStack existing = filterHandler.extractItem(0, 1, false);
            if (!existing.isEmpty()) {
                if (!player.getInventory().add(existing)) {
                    player.drop(existing, false);
                }
                return true;
            }
            return false;
        }
        if (heldStack.getItem() instanceof MeshItem) {
            ItemStack one = heldStack.copyWithCount(1);
            ItemStack remainder = filterHandler.insertItem(0, one, false);
            if (remainder.isEmpty()) {
                if (!player.getAbilities().instabuild) {
                    heldStack.shrink(1);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Filter")) {
            filterHandler.deserializeNBT(registries, tag.getCompound("Filter"));
        }
        if (tag.contains("Output")) {
            outputHandler.deserializeNBT(registries, tag.getCompound("Output"));
        }
        tickCounter = tag.getInt("Tick");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Filter", filterHandler.serializeNBT(registries));
        tag.put("Output", outputHandler.serializeNBT(registries));
        tag.putInt("Tick", tickCounter);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.put("Filter", filterHandler.serializeNBT(registries));
        tag.put("Output", outputHandler.serializeNBT(registries));
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains("Filter")) {
            filterHandler.deserializeNBT(registries, tag.getCompound("Filter"));
        }
        if (tag.contains("Output")) {
            outputHandler.deserializeNBT(registries, tag.getCompound("Output"));
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void syncToClients() {
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(getBlockPos(), state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static final BlockCapability<IItemHandler, Direction> OUTPUT_CAPABILITY = Capabilities.ItemHandler.BLOCK;

    /** Output-only IItemHandler view: extraction allowed, insertion denied. */
    private static final class OutputOnlyWrapper extends CombinedInvWrapper {
        OutputOnlyWrapper(IItemHandlerModifiable wrapped) {
            super(wrapped);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack;
        }
    }
}
