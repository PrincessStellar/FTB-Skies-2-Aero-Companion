package dev.ftb.mods.ftbskies2aerocompanion.compat.createnewage;

import org.antarcticgardens.cna.content.energising.EnergiserBlock;

public class AeroEnergiserBlock extends EnergiserBlock implements AeroEnergiser {
    private final int tier;

    public AeroEnergiserBlock(Properties properties, int tier) {
        super(properties, tier);
        this.tier = tier;
    }

    @Override
    public int aeroTier() {
        return tier;
    }
}
