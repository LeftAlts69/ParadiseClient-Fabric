package io.github.spigotrce.paradiseclientfabric.chatroom.server.netty;

import io.github.spigotrce.paradiseclientfabric.chatroom.common.exception.BadPacketException;
import io.github.spigotrce.paradiseclientfabric.chatroom.common.packet.Packet;
import io.github.spigotrce.paradiseclientfabric.chatroom.common.packet.PacketRegistry;
import io.github.spigotrce.paradiseclientfabric.chatroom.common.packet.impl.DisconnectPacket;
import io.github.spigotrce.paradiseclientfabric.chatroom.common.packet.impl.MessagePacket;
import io.github.spigotrce.paradiseclientfabric.chatroom.server.Logging;
import io.github.spigotrce.paradiseclientfabric.chatroom.server.handler.ServerPacketHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ChatRoomServerHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private ServerPacketHandler packetHandler;
    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        Packet packet = PacketRegistry.createAndDecode(msg.readInt(), msg);
        if (packet == null) throw new BadPacketException("Unknown packet");
        packet.handle(packetHandler);
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        packetHandler = new ServerPacketHandler(ctx.channel());
        ChatRoomServer.channels.add(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ChatRoomServer.channels.remove(ctx.channel());
        if (packetHandler.userModel != null) {
            Logging.info("Disconnection: " + packetHandler.userModel.username() + "/" + ctx.channel().remoteAddress());
            ChatRoomServer.channels.forEach(channel -> PacketRegistry.sendPacket(new MessagePacket(packetHandler.userModel.username() + " left the chat"), channel));
        }
        packetHandler = null;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Logging.error("Exception caught on netty thread", cause);
        PacketRegistry.sendPacket(new DisconnectPacket("Error in netty thread, check server console."), ctx.channel());
        ctx.close();
    }
}
