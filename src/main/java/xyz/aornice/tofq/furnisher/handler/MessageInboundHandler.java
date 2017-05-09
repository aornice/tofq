package xyz.aornice.tofq.furnisher.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.TypeParameterMatcher;
import xyz.aornice.tofq.furnisher.message.MessageBuilder;

public abstract class MessageInboundHandler<M> extends ChannelInboundHandlerAdapter {

    private final TypeParameterMatcher matcher;

    protected MessageInboundHandler() {
        matcher = TypeParameterMatcher.find(this, MessageInboundHandler.class, "M");
    }

    public boolean acceptInboundMessage(Object msg) throws Exception {
        if (!(msg instanceof MessageBuilder.Message)) return false;
        @SuppressWarnings("unchecked")
        MessageBuilder.Message imsg = (MessageBuilder.Message) msg;
        return matcher.match(imsg.getPayload());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (acceptInboundMessage(msg)) {
            try {
                @SuppressWarnings("unchecked")
                MessageBuilder.Message message = (MessageBuilder.Message) msg;
                @SuppressWarnings("unchecked")
                M imsg = (M) message.getPayload();
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
