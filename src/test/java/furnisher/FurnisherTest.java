package furnisher;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.*;

import xyz.aornice.tofq.depostion.CargoDeposition;
import xyz.aornice.tofq.depostion.support.LocalDeposition;
import xyz.aornice.tofq.furnisher.codec.FurnisherMessageDecoder;
import xyz.aornice.tofq.furnisher.codec.FurnisherMessageEncoder;
import xyz.aornice.tofq.furnisher.codec.FurnisherVarint32FrameDecoder;
import xyz.aornice.tofq.furnisher.codec.FurnisherVarint32LengthFieldPrepender;
import xyz.aornice.tofq.furnisher.handler.PutHandler;
import xyz.aornice.tofq.furnisher.message.Message;
import xyz.aornice.tofq.furnisher.message.MessageBuilder;
import xyz.aornice.tofq.furnisher.message.Operation;
import xyz.aornice.tofq.furnisher.message.payload.*;
import xyz.aornice.tofq.furnisher.util.Varint32;
import xyz.aornice.tofq.furnisher.util.support.ArraySortedMap;
import xyz.aornice.tofq.utils.impl.LocalTopicCenter;

import java.nio.charset.StandardCharsets;


public class FurnisherTest {

    private static final Logger logger = LogManager.getLogger(FurnisherTest.class);

    MessageBuilder msgBuilder = new MessageBuilder();
    PutBuilder putBuilder = new PutBuilder();
    PutResponseBuilder putRespBuilder = new PutResponseBuilder();
    String topic = "faketopic";

    //    @Test
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

    @Test
    public void testInbound() {
        EmbeddedChannel ch = new EmbeddedChannel(
                new FurnisherVarint32FrameDecoder(),
                new FurnisherMessageDecoder()
        );

        Message oMsg = msgBuilder.build(
                Operation.PUT,
                putBuilder.build(topic, 2, Unpooled.copiedBuffer("Hello World", CharsetUtil.UTF_8))
        );
        assertTrue(ch.writeInbound(putMsgBuf(oMsg)));
        assertTrue(ch.finish());

        Message msg = ch.readInbound();

        assertEquals(msg.getOp(), oMsg.getOp());
        Put put = (Put) msg.getPayload();
        Put oPut = (Put) oMsg.getPayload();
        assertEquals(put.getTopic(), oPut.getTopic());
        assertEquals(put.getSeq(), oPut.getSeq());
        assertEquals(put.getData(), oPut.getData());
    }

    @Test
    public void testOutbound() throws InterruptedException {
        Message oMsg = msgBuilder.build(
                Operation.PUT_RESP,
                putRespBuilder.build(topic, 2)
        );
        oMsg.retain();
        EmbeddedChannel ch = new EmbeddedChannel(
                new FurnisherVarint32LengthFieldPrepender(),
                new FurnisherMessageEncoder()
        );

        ch.writeOutbound(oMsg);
        assertTrue(ch.finish());
        ByteBuf in = ch.readOutbound();

        Varint32.readRawVarint32(in);

        assertEquals(oMsg.getOp().value(), Varint32.readRawVarint32(in));
        int topicLen = Varint32.readRawVarint32(in);
        ByteBuf topicBuf = Unpooled.buffer();
        in.readBytes(topicBuf, topicLen);
        assertEquals(topic, topicBuf.toString(CharsetUtil.US_ASCII));
        assertEquals(((PutResponse) oMsg.getPayload()).getSeq(), Varint32.readRawVarint32(in));

    }

    @Test
    public void testBidirecttion() throws InterruptedException {
        LocalTopicCenter.getInstance().register(topic);
        EmbeddedChannel ch = new EmbeddedChannel(
                new FurnisherVarint32FrameDecoder(),
                new FurnisherVarint32LengthFieldPrepender(),
                new FurnisherMessageDecoder(),
                new FurnisherMessageEncoder(),
                new PutHandler()
        );
        CargoDeposition deposition = LocalDeposition.getInstance();
        deposition.start();

        int end = 5;
        for (int i = 0; i < end; i++) {
            ch.writeInbound(putMsgBuf(msgBuilder.build(
                    Operation.PUT,
                    putBuilder.build(topic, i, Unpooled.copiedBuffer("Hello world", CharsetUtil.UTF_8))
            )));
        }

        deposition.shutdownGracefully();
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            if (t.isDaemon() || !t.getName().equals("DepositionTask")) continue;
            t.join();
        }

        ch.finish();
        ByteBuf in = null;

        for (;;) {
            in = ch.readOutbound();
            assertNotNull(in);
            Varint32.readRawVarint32(in);
            assertEquals(Operation.PUT_RESP.value(), Varint32.readRawVarint32(in));
            int topicLen = Varint32.readRawVarint32(in);
            ByteBuf topicBuf = Unpooled.buffer();
            in.readBytes(topicBuf, topicLen);
            assertEquals(topic, topicBuf.toString(CharsetUtil.US_ASCII));

            if (end - 1 == Varint32.readRawVarint32(in)) {
                break;
            }
        }


    }

    static ByteBuf putMsgBuf(Message msg) {

        Put oPut = (Put) msg.getPayload();

        ByteBuf buf = Unpooled.buffer();

        // write msg
        ByteBuf msgbuf = Unpooled.buffer();
        Varint32.writeRawVarint32(msgbuf, msg.getOp().value());

        Varint32.writeRawVarint32(msgbuf, oPut.getTopic().length());
        msgbuf.writeBytes(oPut.getTopic().getBytes(StandardCharsets.US_ASCII));

        Varint32.writeRawVarint32(msgbuf, oPut.getSeq());

        msgbuf.writeBytes(oPut.getData().duplicate());

        // write len + msg
        int msgLen = msgbuf.readableBytes();
        Varint32.writeRawVarint32(buf, msgLen);
        buf.writeBytes(msgbuf);
        return buf;
    }

    class SimpleHandler extends SimpleChannelInboundHandler<ByteBuf> {

        ByteBuf buf;

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
            buf = msg.retain();
            ctx.write(msg.retain());
        }
    }

}
