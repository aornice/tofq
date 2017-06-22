package xyz.aornice.tofq.furnisher.message;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import xyz.aornice.tofq.furnisher.message.payload.Payload;
import xyz.aornice.tofq.furnisher.util.Varint32;
import xyz.aornice.tofq.furnisher.util.support.AbstractBuilder;

@ChannelHandler.Sharable
public class MessageBuilder extends AbstractBuilder<Message> {

    public MessageBuilder() {
        super();
    }

    public MessageBuilder(int initial) {
        super(initial);
    }

    public Message build(Operation op, Payload payload) {
        return ((MessageImpl) build()).setOp(op).setPayload(payload);
    }

    @Override
    protected Message buildPartial() {
        return new MessageImpl();
    }

    @Override
    public Message build(ByteBuf in) throws Exception {
        Operation op = Operation.valueOf(Varint32.readRawVarint32(in));
        return build(op, op.getBuilder().build(in));
    }

    class MessageImpl extends AbstractBuilder.ElementAdapter implements Message {
        private Operation op;
        private Payload payload;

        MessageImpl() {
        }

        @Override
        public void reset() {
            op = null;
            payload = null;
        }

        @Override
        public Operation getOp() {
            return op;
        }

        @Override
        public Payload getPayload() {
            return payload;
        }

        @Override
        public void write(ByteBuf out) {
            Varint32.writeRawVarint32(out, op.value());
            payload.write(out);
        }

        @Override
        protected void releaseHelper() {
            payload.release();
        }

        @Override
        protected void releaseDeepHelper() {
            payload.releaseDeep();
        }

        MessageImpl setOp(Operation op) {
            this.op = op;
            return this;
        }

        MessageImpl setPayload(Payload payload) {
            this.payload = payload;
            return this;
        }

    }

}
