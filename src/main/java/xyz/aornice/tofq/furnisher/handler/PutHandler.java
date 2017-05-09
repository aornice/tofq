package xyz.aornice.tofq.furnisher.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import xyz.aornice.tofq.furnisher.message.payload.PutBuilder;


@ChannelHandler.Sharable
public class PutHandler extends MessageInboundHandler<PutBuilder.Put> {
    @Override
    public void messageReceived(ChannelHandlerContext cxt, PutBuilder.Put msg) throws Exception {
    }
}
