package xyz.aornice.tofq.furnisher.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.aornice.tofq.furnisher.util.Varint32;


@ChannelHandler.Sharable
public class FurnisherVarint32LengthFieldPrepender extends MessageToByteEncoder<ByteBuf> {

    private static final Logger logger = LogManager.getLogger(FurnisherVarint32LengthFieldPrepender.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out)
            throws Exception {
        int bodyLen = msg.readableBytes();
        int headerLen = Varint32.computeRawVarint32Size(bodyLen);
        out.ensureWritable(headerLen + bodyLen);
        Varint32.writeRawVarint32(out, bodyLen);
        out.writeBytes(msg, msg.readerIndex(), bodyLen);
    }
}
