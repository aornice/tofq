package xyz.aornice.tofq.furnisher.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.aornice.tofq.furnisher.message.Message;

import java.util.List;

@ChannelHandler.Sharable
public class FurnisherMessageEncoder extends MessageToMessageEncoder<Message> {

    private static final Logger logger = LogManager.getLogger(FurnisherMessageEncoder.class);

    private final PooledByteBufAllocator allocator = new PooledByteBufAllocator();

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        ByteBuf buf = allocator.buffer();
        msg.write(buf);
        out.add(buf);
    }
}
