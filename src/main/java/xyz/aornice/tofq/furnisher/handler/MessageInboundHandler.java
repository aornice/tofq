package xyz.aornice.tofq.furnisher.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.TypeParameterMatcher;
import xyz.aornice.tofq.furnisher.message.Message;
import xyz.aornice.tofq.furnisher.message.payload.Payload;
import xyz.aornice.tofq.furnisher.util.Recyclable;

public abstract class MessageInboundHandler<M extends Payload> extends ChannelInboundHandlerAdapter {

    private final TypeParameterMatcher matcher;

    protected MessageInboundHandler() {
        matcher = TypeParameterMatcher.find(this, MessageInboundHandler.class, "M");
    }

    public boolean acceptInboundMessage(Object msg) throws Exception {
        if (!(msg instanceof Message)) return false;
        @SuppressWarnings("unchecked")
        Message imsg = (Message) msg;
        return matcher.match(imsg.getPayload());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (acceptInboundMessage(msg)) {
            M imsg = null;
            try {
                @SuppressWarnings("unchecked")
                Message message = (Message) msg;
                imsg = (M) message.getPayload();
                messageReceived(ctx, imsg);
            } finally {
                ReferenceCountUtil.release(msg);
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    abstract public void messageReceived(ChannelHandlerContext cxt, M msg) throws Exception;

}
