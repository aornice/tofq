package furnisher;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.CharsetUtil;
import org.junit.Test;

import static org.junit.Assert.*;

import xyz.aornice.tofq.furnisher.codec.FurnisherMessageDecoder;
import xyz.aornice.tofq.furnisher.codec.FurnisherVarint32FrameDecoder;
import xyz.aornice.tofq.furnisher.message.Message;
import xyz.aornice.tofq.furnisher.message.MessageBuilder;
import xyz.aornice.tofq.furnisher.message.Operation;
import xyz.aornice.tofq.furnisher.message.payload.Put;
import xyz.aornice.tofq.furnisher.message.payload.PutBuilder;
import xyz.aornice.tofq.furnisher.util.Varint32;
import xyz.aornice.tofq.furnisher.util.support.ArraySortedMap;

public class FurnisherTest {

    MessageBuilder msgBuilder = new MessageBuilder();
    PutBuilder putBuilder = new PutBuilder();

    @Test
    public void testArraySortedMap() {
        ArraySortedMap a = new ArraySortedMap(4);
        a.add(0, 0);
        a.add(1, 1);
        a.add(3, 3);
        a.add(4, 4);
        assertEquals(4, a.size());
        assertEquals(1, a.findLEAndClear(2));
        assertEquals(2, a.size());
        a.add(6, 6);
        a.add(7, 7);
        assertEquals(a.size(), 4);
        a.add(9, 9);
        a.add(10, 10);
        a.add(11, 11);
        assertEquals(7, a.size());
        assertEquals(10, a.findLEAndClear(10));
        assertEquals(1, a.size());
    }

//    @Test
    public void testMessageDecoder() {


        Message oMsg = msgBuilder.build(
                Operation.PUT,
                putBuilder.build("faketopic", 2, Unpooled.copiedBuffer("Hello World", CharsetUtil.UTF_8))
        );
        Put oPut = (Put) oMsg.getPayload();

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

        Message msg = ch.readInbound();

        assertEquals(msg.getOp(), oMsg.getOp());
        Put put = (Put) msg.getPayload();
        assertEquals(put.getTopic(), oPut.getTopic());
        assertEquals(put.getSeq(), oPut.getSeq());
        assertEquals(put.getData(), oPut.getData());
    }
}
