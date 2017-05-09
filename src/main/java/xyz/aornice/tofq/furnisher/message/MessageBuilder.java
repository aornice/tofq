package xyz.aornice.tofq.furnisher.message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import xyz.aornice.tofq.furnisher.util.Recyclable;
import xyz.aornice.tofq.furnisher.util.Varint32;
import xyz.aornice.tofq.furnisher.util.support.AbstractBuilder;

@ChannelHandler.Sharable
public class MessageBuilder extends AbstractBuilder<MessageBuilder.Message> {

    public MessageBuilder() {
        super();
    }

    public MessageBuilder(int initial) {
        super(initial);
    }

    public Message build(Operation op, Recyclable payload) {
        return build().setOp(op).setPayload(payload);
    }

    @Override
    protected Message buildPartial() {
        return new Message();
    }

    @Override
    public Message build(ByteBuf in) throws Exception{
        Operation op = Operation.valueOf(Varint32.readRawVarint32(in));
        return build(op, op.getBuilder().build(in));
    }

    public class Message extends AbstractBuilder.ElementAdapter {
        private Operation op;
        private Recyclable payload;

        Message() {
        }

        @Override
        public void reset() {
            op = null;
            payload = null;
        }

        public Operation getOp() {
            return op;
        }

        public Recyclable getPayload() {
            return payload;
        }

        @Override
        protected void releaseDeepHelper() {
            payload.releaseDeep();
        }

        Message setOp(Operation op) {
            this.op = op;
            return this;
        }

        Message setPayload(Recyclable payload) {
            this.payload = payload;
            return this;
        }

    }

}
