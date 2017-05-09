package furnisher;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.CharsetUtil;
import org.junit.Test;

import static org.junit.Assert.*;

import xyz.aornice.tofq.furnisher.codec.FurnisherMessageDecoder;
import xyz.aornice.tofq.furnisher.codec.FurnisherVarint32FrameDecoder;
import xyz.aornice.tofq.furnisher.message.MessageBuilder;
import xyz.aornice.tofq.furnisher.message.Operation;
import xyz.aornice.tofq.furnisher.message.payload.PutBuilder;
import xyz.aornice.tofq.furnisher.util.Varint32;

public class FurnisherTest {

    MessageBuilder msgBuilder = new MessageBuilder();
    PutBuilder putBuilder = new PutBuilder();

    @Test
    public void testMessageDecoder() {


        MessageBuilder.Message oMsg = msgBuilder.build(
                Operation.PUT,
                putBuilder.build("faketopic", 2, Unpooled.copiedBuffer("Hello World", CharsetUtil.UTF_8))
        );
        PutBuilder.Put oPut = (PutBuilder.Put) oMsg.getPayload();

        ByteBuf buf = Unpooled.buffer();

        // write msg
        ByteBuf msgbuf = Unpooled.buffer();
        Varint32.writeRawVarint32(msgbuf, oMsg.getOp().value());

        ByteBuf topic = Unpooled.copiedBuffer(oPut.getTopic(), CharsetUtil.UTF_8);
        Varint32.writeRawVarint32(msgbuf, topic.readableBytes());
        msgbuf.writeBytes(topic);

        Varint32.writeRawVarint32(msgbuf, oPut.getSeq());

        msgbuf.writeBytes(oPut.getData().duplicate());

        // write len + msg
        int msgLen = msgbuf.readableBytes();
        Varint32.writeRawVarint32(buf, msgLen);
        buf.writeBytes(msgbuf);

        EmbeddedChannel ch = new EmbeddedChannel(
                new FurnisherVarint32FrameDecoder(),
                new FurnisherMessageDecoder()
        );

        assertTrue(ch.writeInbound(buf.duplicate()));
        assertTrue(ch.finish());

        MessageBuilder.Message msg = ch.readInbound();

        assertEquals(msg.getOp(), oMsg.getOp());
        PutBuilder.Put put = (PutBuilder.Put) msg.getPayload();
        assertEquals(put.getTopic(), oPut.getTopic());
        assertEquals(put.getSeq(), oPut.getSeq());
        assertEquals(put.getData(), oPut.getData());
    }
}
