package dev.ftb.mods.ftbskies2aerocompanion.compat.jade;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.AeroScoopBlock;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.AeroScoopBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.JadeIds;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

import java.util.ArrayList;
import java.util.List;

@WailaPlugin(FTBSkies2AeroCompanion.MOD_ID)
public class AeroScoopJadePlugin implements IWailaPlugin {
    public static final ResourceLocation FILTER_UID = FTBSkies2AeroCompanion.id("aeroscoop_filter");
    public static final ResourceLocation OUTPUT_UID = FTBSkies2AeroCompanion.id("aeroscoop_output");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerEntityDataProvider(new AeroScoopContraptionProvider.ServerData(),
                AbstractContraptionEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new FilterProvider(), AeroScoopBlock.class);
        registration.registerBlockComponent(new OutputProvider(), AeroScoopBlock.class);

        registration.registerEntityComponent(new AeroScoopContraptionProvider.FilterComponent(),
                AbstractContraptionEntity.class);
        registration.registerEntityComponent(new AeroScoopContraptionProvider.OutputComponent(),
                AbstractContraptionEntity.class);

        // The AeroScoop exposes its buffer as an IItemHandler capability so pipes/hoppers can extract.
        // Jade's universal item-storage component picks that up too, producing a duplicate list — strip it.
        registration.addTooltipCollectedCallback((box, accessor) -> {
            if (accessor instanceof BlockAccessor ba && ba.getBlock() instanceof AeroScoopBlock) {
                box.getTooltip().remove(JadeIds.UNIVERSAL_ITEM_STORAGE);
            }
        });
    }

    private static final class FilterProvider implements IBlockComponentProvider {
        @Override
        public ResourceLocation getUid() {
            return FILTER_UID;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (!(accessor.getBlockEntity() instanceof AeroScoopBlockEntity be)) return;
            AeroScoopJadeRows.appendFilter(tooltip, be.getFilter());
        }
    }

    private static final class OutputProvider implements IBlockComponentProvider {
        @Override
        public ResourceLocation getUid() {
            return OUTPUT_UID;
        }

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (!(accessor.getBlockEntity() instanceof AeroScoopBlockEntity be)) return;
            List<ItemStack> stacks = new ArrayList<>(be.outputHandler.getSlots());
            for (int i = 0; i < be.outputHandler.getSlots(); i++) {
                stacks.add(be.outputHandler.getStackInSlot(i));
            }
            AeroScoopJadeRows.appendOutputs(tooltip, stacks);
        }
    }
}
