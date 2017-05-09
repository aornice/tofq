package xyz.aornice.tofq.furnisher.message.payload;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import xyz.aornice.tofq.furnisher.util.Varint32;
import xyz.aornice.tofq.furnisher.util.support.AbstractBuilder;
import xyz.aornice.tofq.util.Pool;


public class PutBuilder extends AbstractBuilder<PutBuilder.Put> {


    public PutBuilder() {
        super();
    }

    public PutBuilder(int initial) {
        super(initial);
    }

    public Put build(String topic, int seq, ByteBuf data) {
        return build().setTopic(topic).setSeq(seq).setData(data);
    }

    @Override
    protected Put buildPartial() {
        return new Put();
    }

    @Override
    public Put build(ByteBuf in) throws Exception {
        Put msg = build();

        int topicLen = Varint32.readRawVarint32(in);
        msg.setTopic(in.readCharSequence(topicLen, CharsetUtil.UTF_8).toString());

        msg.setSeq(Varint32.readRawVarint32(in));

        msg.setData(in.retainedSlice(in.readerIndex(), in.readableBytes()));

        return msg;
    }

    public class Put extends AbstractBuilder.ElementAdapter {
        private String topic;
        private int seq;
        private ByteBuf data;

        Put() {
        }

        @Override
        public void reset() {
            seq = -1;
            data = null;
        }

        public String getTopic() {
            return topic;
        }

        public int getSeq() {
            return seq;
        }

        public ByteBuf getData() {
            return data;
        }

        @Override
        protected void releaseDeepHelper() {
            data.release();
        }

        Put setTopic(String topic) {
            this.topic = topic;
            return this;
        }

        Put setSeq(int seq) {
            this.seq = seq;
            return this;
        }

        Put setData(ByteBuf data) {
            this.data = data;
            return this;
        }

    }

}

