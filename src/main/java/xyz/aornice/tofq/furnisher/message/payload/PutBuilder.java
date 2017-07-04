package xyz.aornice.tofq.furnisher.message.payload;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import xyz.aornice.tofq.furnisher.util.Varint32;
import xyz.aornice.tofq.furnisher.util.support.AbstractBuilder;


public class PutBuilder extends PayloadAbstractBuilder<Put> {

    public PutBuilder() {
        super();
    }

    public PutBuilder(int initial) {
        super(initial);
    }

    public Put build(String topic, int seq, ByteBuf data) {
        return ((PutImpl) build()).setTopic(topic).setSeq(seq).setData(data);
    }

    @Override
    protected Put buildPartial() {
        return new PutImpl();
    }

    @Override
    public Put build(ByteBuf in) throws Exception {
        final int topicLen = Varint32.readRawVarint32(in);
        return build(
                in.readCharSequence(topicLen, CharsetUtil.US_ASCII).toString(),
                Varint32.readRawVarint32(in),
                in.retainedSlice(in.readerIndex(), in.readableBytes())
        );
    }

    class PutImpl extends AbstractBuilder.ElementAdapter implements Put {
        private String topic;
        private int seq;
        private ByteBuf data;

        PutImpl() {
        }

        @Override
        public void reset() {
            seq = -1;
            data = null;
        }

        @Override
        public String getTopic() {
            return topic;
        }

        @Override
        public int getSeq() {
            return seq;
        }

        @Override
        public ByteBuf getData() {
            return data;
        }

        @Override
        public void write(ByteBuf out) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void releaseDeepHelper() {
            data.release();
        }

        PutImpl setTopic(String topic) {
            this.topic = topic;
            return this;
        }

        PutImpl setSeq(int seq) {
            this.seq = seq;
            return this;
        }

        PutImpl setData(ByteBuf data) {
            this.data = data;
            return this;
        }
    }

}

