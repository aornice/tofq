package xyz.aornice.tofq.furnisher.message.payload;

import io.netty.buffer.ByteBuf;
import xyz.aornice.tofq.furnisher.util.Varint32;

import java.nio.charset.StandardCharsets;


public class PutResponseBuilder extends PayloadAbstractBuilder<PutResponse> {

    @Override
    public PutResponse build(ByteBuf in) throws Exception {
        throw new UnsupportedOperationException();
    }

    public PutResponse build(String topic, int seq) {
        return ((PutResponseImpl) build()).setTopic(topic).setSeq(seq);
    }

    @Override
    protected PutResponse buildPartial() {
        return new PutResponseImpl();
    }

    class PutResponseImpl extends ElementAdapter implements PutResponse {

        private String topic;
        private int seq;

        @Override
        public boolean releaseDeep() {
            return false;
        }

        @Override
        public void reset() {}

        @Override
        public String getTopic() {
            return topic;
        }

        @Override
        public int getSeq() {
            return seq;
        }

        @Override
        public void write(ByteBuf out) {
            Varint32.writeRawVarint32(out, topic.length());
            out.writeBytes(topic.getBytes(StandardCharsets.US_ASCII));
            Varint32.writeRawVarint32(out, seq);
        }

        PutResponseImpl setTopic(String topic) {
            this.topic = topic;
            return this;
        }

        PutResponseImpl setSeq(int seq) {
            this.seq = seq;
            return this;
        }
    }
}
