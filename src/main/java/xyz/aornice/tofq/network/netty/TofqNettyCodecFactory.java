package xyz.aornice.tofq.network.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.aornice.tofq.network.codec.Codec;
import xyz.aornice.tofq.network.command.Command;

import java.nio.ByteBuffer;

/**
 * codec factory
 * Created by drfish on 09/05/2017.
 */
public class TofqNettyCodecFactory {
    private static final Logger logger = LoggerFactory.getLogger(TofqNettyCodecFactory.class);

    /**
     * the codec used in C/S encode and decode process
     */
    private Codec codec;

    public TofqNettyCodecFactory(Codec codec) {
        this.codec = codec;
    }

    /***
     * the encoder
     */
    public class NettyEncoder extends MessageToByteEncoder<Command> {
        @Override
        protected void encode(ChannelHandlerContext ctx, Command command, ByteBuf out) throws Exception {
            try {
                ByteBuffer byteBuffer = codec.encode(command);
                out.writeBytes(byteBuffer);
            } catch (Exception e) {
                logger.error("{} encode exception", ctx.channel().remoteAddress());
                if (command != null) {
                    logger.error(command.toString());
                }
                ctx.channel().close();
            }
        }
    }

    /**
     * the decoder
     */
    public class NettyDecoder extends LengthFieldBasedFrameDecoder {
        // TODO make this configurable
        private static final int FRAME_MAX_LENGTH = 1 << 20;

        public NettyDecoder() {
            super(FRAME_MAX_LENGTH, 0, 4, 0, 4);
        }

        @Override
        protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
            ByteBuf frame = null;
            try {
                frame = (ByteBuf) super.decode(ctx, in);
                if (frame == null) {
                    return null;
                }
                ByteBuffer byteBuffer = frame.nioBuffer();
                return codec.decode(byteBuffer);
            } catch (Exception e) {
                logger.error("{} decode exception", ctx.channel().remoteAddress());
                ctx.channel().close();
            } finally {
                if (frame != null) {
                    frame.release();
                }
            }
            return null;
        }
    }

    public ChannelHandler getDecoder() {
        return new NettyDecoder();
    }

    public ChannelHandler getEncoder() {
        return new NettyEncoder();
    }
}
