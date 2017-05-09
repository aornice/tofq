package xyz.aornice.tofq.furnisher.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import xyz.aornice.tofq.furnisher.message.MessageBuilder;

import java.util.List;


public class FurnisherMessageDecoder extends MessageToMessageDecoder<ByteBuf> {

    private MessageBuilder builder;

    public FurnisherMessageDecoder() {
        builder = new MessageBuilder(100);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out)
            throws Exception {
        out.add(builder.build(msg));
    }
}
