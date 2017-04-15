package xyz.aornice.tofq.depostion;

import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.Topic;

/**
 * Created by robin on 15/04/2017.
 */
public class ICargo {
    private Topic topic;
    private long id;
    private byte[] data;

    public ICargo(Topic topic, long id, byte[] data) {
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

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int size() {
        return data.length;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Cargo)) return false;
        Cargo other = (Cargo) obj;
        return topic.equals(other.getTopic()) && (id == other.getId());
    }

    public int hashCode() {
        return topic.hashCode() ^ (int)(id >>> 32) ^ (int)(id & (((long)1 << 33) - 1));
    }
}
