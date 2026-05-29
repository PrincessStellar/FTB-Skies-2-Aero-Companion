package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.sable;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.logging.LogUtils;
import dev.ryanhcode.sable.sublevel.plot.SubLevelPlayerChunkSender;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Consumer;

/**
 * Serializing a sub-level chunk to a tracking player runs every block entity's
 * {@code getUpdateTag}. A block entity that throws there (e.g. an EnderIO conduit whose
 * network node has not formed yet on a freshly loaded sub-level) propagates out of the
 * sub-level tick and crashes the server. Contain it: a single misbehaving block entity
 * should drop that chunk's sync for the moment (it re-syncs once its data is valid), not
 * take down the world tick.
 */
@Mixin(value = SubLevelPlayerChunkSender.class, remap = false)
public abstract class SubLevelPlayerChunkSenderMixin {

    private static final Logger LOGGER = LogUtils.getLogger();

    @WrapMethod(method = "sendChunk")
    private static void ftbskies2aero$guardChunkSend(Consumer<Packet<? super ClientGamePacketListener>> listener,
                                                     LevelLightEngine lightEngine, LevelChunk chunk,
                                                     Operation<Void> original) {
        try {
            original.call(listener, lightEngine, chunk);
        } catch (Throwable t) {
            LOGGER.error("Skipped sub-level chunk sync at {} due to a block entity error", chunk.getPos(), t);
        }
    }
}
