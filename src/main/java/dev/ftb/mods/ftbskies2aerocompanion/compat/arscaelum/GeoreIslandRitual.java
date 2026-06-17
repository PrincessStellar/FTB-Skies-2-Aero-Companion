package dev.ftb.mods.ftbskies2aerocompanion.compat.arscaelum;

import com.hollingsworth.arsnouveau.api.ritual.StructureRitual;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class GeoreIslandRitual extends StructureRitual {

    private final ResourceLocation registryName;
    private final String langName;
    private final String langDescription;

    public GeoreIslandRitual(ResourceLocation registryName, ResourceLocation structure, String langName, String langDescription) {
        super(structure, new BlockPos(-8, -3, -7), 10000, null);
        this.registryName = registryName;
        this.langName = langName;
        this.langDescription = langDescription;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return registryName;
    }

    @Override
    public String getLangName() {
        return langName;
    }

    @Override
    public String getLangDescription() {
        return langDescription;
    }
}
