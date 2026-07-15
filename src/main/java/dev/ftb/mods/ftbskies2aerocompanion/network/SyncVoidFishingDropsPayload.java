package dev.ftb.mods.ftbskies2aerocompanion.network;

import dev.ftb.mods.ftbskies2aerocompanion.FTBSkies2AeroCompanion;
import dev.ftb.mods.ftbskies2aerocompanion.compat.jei.VoidFishingDrop;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

/**
 * S2C payload carrying the server's resolved void-fishing drops. Multiplayer clients have no other way
 * to see datapack-overridden loot tables, so we ship the resolved list directly on player login.
 */
public record SyncVoidFishingDropsPayload(List<VoidFishingDrop> drops) implements CustomPacketPayload {
    public static final Type<SyncVoidFishingDropsPayload> TYPE =
            new Type<>(FTBSkies2AeroCompanion.id("sync_void_fishing_drops"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncVoidFishingDropsPayload> STREAM_CODEC =
            StreamCodec.composite(
                    VoidFishingDrop.STREAM_CODEC.apply(ByteBufCodecs.list()), SyncVoidFishingDropsPayload::drops,
                    SyncVoidFishingDropsPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
