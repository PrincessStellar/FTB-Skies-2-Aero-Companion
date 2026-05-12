package dev.ftb.mods.ftbskies2aerocompanion.compat.ponder;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import dev.ftb.mods.ftbskies2aerocompanion.aeroscoop.ModBlocks;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class AeroScoopPonderPlugin implements PonderPlugin {
    @Override
    public String getModId() {
        return FTBSkies2AeroCompanion.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        ResourceLocation block = BuiltInRegistries.BLOCK.getKey(ModBlocks.AIR_FILTER.get());
        helper.forComponents(block)
                .addStoryBoard("aeroscoop", AeroScoopScenes::aeroScoop);
    }
}
