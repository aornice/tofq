package xyz.aornice.tofq;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by robin on 10/04/2017.
 */
public class Cargo implements Identifiable {
    private Topic topic;
    private long id;
    private ByteBuf data;

    public Cargo() {
    }

    public Cargo(Topic topic, long id, byte[] data) {
        this.topic = topic;
        this.id = id;
        this.data = Unpooled.wrappedBuffer(data);
    }

    public Cargo(Topic topic, long id, ByteBuf data) {
        this.topic = topic;
        this.id = id;
        this.data = data;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ByteBuf getData() {
        return data.retainedSlice();
    }

    public ByteBuffer getDataByteBuffer() {
        return data.nioBuffer();
    }

    public byte[] getDataArray() {
        byte[] bytes = new byte[data.readableBytes()];
        data.getBytes(data.readerIndex(), bytes);
        return bytes;
    }

    public void setData(ByteBuf data) {
        this.data = data;
    }

    public void setData(byte[] data) {
        this.data = Unpooled.wrappedBuffer(data);
    }

    public int size() {
        return data.readableBytes();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Cargo)) return false;
        Cargo other = (Cargo) obj;
        return topic.equals(other.getTopic()) && (id == other.getId());
    }

    public int hashCode() {
        return topic.hashCode() ^ (int) (id >>> 32) ^ (int) (id & (((long) 1 << 33) - 1));
    }

    @Override
    public String toString() {
        String description = String.format("topic: %s, id: %d, data: %s\n", topic.getName(), id, Arrays.toString(getDataArray()));
        return description;
    }
}
