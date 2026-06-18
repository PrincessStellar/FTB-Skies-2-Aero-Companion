package dev.ftb.mods.ftbskies2aerocompanion.mixin.compat.sable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ryanhcode.sable.network.packets.tcp.ClientboundSableUDPActivationPacket;
import io.netty.channel.Channel;
import io.netty.channel.EventLoop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ClientboundSableUDPActivationPacket.class, remap = false)
public class ClientboundSableUDPActivationPacketMixin {

    @WrapOperation(method = "handle", at = @At(value = "INVOKE", target = "Lio/netty/channel/Channel;eventLoop()Lio/netty/channel/EventLoop;"))
    private EventLoop ftbskies2aero$guardNullChannel(Channel channel, Operation<EventLoop> original) {
        return channel == null ? null : original.call(channel);
    }

    @WrapOperation(method = "handle", at = @At(value = "INVOKE", target = "Lio/netty/channel/EventLoop;execute(Ljava/lang/Runnable;)V"))
    private void ftbskies2aero$skipWhenNoEventLoop(EventLoop eventLoop, Runnable task, Operation<Void> original) {
        if (eventLoop != null) {
            original.call(eventLoop, task);
        }
    }
}
