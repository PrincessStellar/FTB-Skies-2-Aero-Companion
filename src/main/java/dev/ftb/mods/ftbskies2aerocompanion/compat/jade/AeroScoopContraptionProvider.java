package dev.ftb.mods.ftbskies2aerocompanion.compat.jade;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.foundation.utility.RaycastHelper;
import com.simibubi.create.foundation.utility.RaycastHelper.PredicateTraceResult;
import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.AeroScoopBlock;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.AeroScoopBlockEntity;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.apache.commons.lang3.mutable.MutableObject;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.ArrayList;
import java.util.List;

public final class AeroScoopContraptionProvider {
    public static final ResourceLocation FILTER_UID = FTBSkies2AeroCompanion.id("aeroscoop_contraption_filter");
    public static final ResourceLocation OUTPUT_UID = FTBSkies2AeroCompanion.id("aeroscoop_contraption_output");
    public static final ResourceLocation SERVER_UID = FTBSkies2AeroCompanion.id("aeroscoop_contraption_data");

    private static final String SD_FILTER = "AeroScoopFilter";
    private static final String SD_OUTPUT = "AeroScoopOutput";
    private static final String SD_HIT = "AeroScoopHit";
    private static final double PROBE_REACH = 8.0;

    private AeroScoopContraptionProvider() {}

    public static final class ServerData implements IServerDataProvider<EntityAccessor> {
        @Override
        public ResourceLocation getUid() {
            return SERVER_UID;
        }

        @Override
        public void appendServerData(CompoundTag tag, EntityAccessor accessor) {
            if (!(accessor.getEntity() instanceof AbstractContraptionEntity entity)) return;
            Player player = accessor.getPlayer();
            if (player == null) return;

            BlockHitResult hit = rayTraceContraption(player, entity);
            if (hit == null) return;
            BlockPos localPos = hit.getBlockPos();

            StructureBlockInfo info = entity.getContraption().getBlocks().get(localPos);
            if (info == null || !(info.state().getBlock() instanceof AeroScoopBlock)) return;
            CompoundTag beNbt = info.nbt();
            if (beNbt == null) return;

            tag.putBoolean(SD_HIT, true);
            if (beNbt.contains("Filter")) tag.put(SD_FILTER, beNbt.getCompound("Filter").copy());
            if (beNbt.contains("Output")) tag.put(SD_OUTPUT, beNbt.getCompound("Output").copy());
        }
    }

    public static final class FilterComponent implements IEntityComponentProvider {
        @Override
        public ResourceLocation getUid() {
            return FILTER_UID;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            CompoundTag sd = accessor.getServerData();
            if (sd == null || !sd.getBoolean(SD_HIT)) return;
            HolderLookup.Provider registries = accessor.getLevel().registryAccess();
            ItemStackHandler filterHandler = new ItemStackHandler(1);
            if (sd.contains(SD_FILTER)) {
                filterHandler.deserializeNBT(registries, sd.getCompound(SD_FILTER));
            }
            AeroScoopJadeRows.appendFilter(tooltip, filterHandler.getStackInSlot(0));
        }
    }

    public static final class OutputComponent implements IEntityComponentProvider {
        @Override
        public ResourceLocation getUid() {
            return OUTPUT_UID;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            CompoundTag sd = accessor.getServerData();
            if (sd == null || !sd.getBoolean(SD_HIT)) return;
            HolderLookup.Provider registries = accessor.getLevel().registryAccess();
            ItemStackHandler outputHandler = new ItemStackHandler(AeroScoopBlockEntity.OUTPUT_SLOTS);
            if (sd.contains(SD_OUTPUT)) {
                outputHandler.deserializeNBT(registries, sd.getCompound(SD_OUTPUT));
            }
            List<ItemStack> stacks = new ArrayList<>(outputHandler.getSlots());
            for (int i = 0; i < outputHandler.getSlots(); i++) {
                stacks.add(outputHandler.getStackInSlot(i));
            }
            AeroScoopJadeRows.appendOutputs(tooltip, stacks);
        }
    }

    // Replicates Create's ContraptionHandlerClient#rayTraceContraption (which is annotated @OnlyIn(CLIENT)
    // on the surrounding class) so it can run from Jade's server-side data provider as well.
    private static BlockHitResult rayTraceContraption(Player player, AbstractContraptionEntity entity) {
        Vec3 origin = player.getEyePosition();
        Vec3 target = origin.add(player.getLookAngle().scale(PROBE_REACH));
        Vec3 localOrigin = entity.toLocalVector(origin, 1);
        Vec3 localTarget = entity.toLocalVector(target, 1);
        Contraption contraption = entity.getContraption();

        MutableObject<BlockHitResult> mutableResult = new MutableObject<>();
        PredicateTraceResult predicateResult = RaycastHelper.rayTraceUntil(localOrigin, localTarget, p -> {
            for (Direction d : Iterate.directions) {
                if (d == Direction.UP) continue;
                BlockPos pos = d == Direction.DOWN ? p : p.relative(d);
                StructureBlockInfo blockInfo = contraption.getBlocks().get(pos);
                if (blockInfo == null) continue;
                BlockState state = blockInfo.state();
                VoxelShape raytraceShape = state.getShape(contraption.getContraptionWorld(), BlockPos.ZERO.below());
                if (raytraceShape.isEmpty()) continue;
                if (contraption.isHiddenInPortal(pos)) continue;
                BlockHitResult rayTrace = raytraceShape.clip(localOrigin, localTarget, pos);
                if (rayTrace != null) {
                    mutableResult.setValue(rayTrace);
                    return true;
                }
            }
            return false;
        });

        if (predicateResult == null || predicateResult.missed()) return null;
        return mutableResult.getValue();
    }
}
