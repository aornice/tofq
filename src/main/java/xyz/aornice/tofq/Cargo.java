package xyz.aornice.tofq;

import java.util.Arrays;

/**
 * Created by robin on 10/04/2017.
 */
public class Cargo implements Identifiable {
    private Topic topic;
    private long id;
    private byte[] data;

    public Cargo() {
    }

    public Cargo(Topic topic, long id, byte[] data) {
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
        return topic.hashCode() ^ (int) (id >>> 32) ^ (int) (id & (((long) 1 << 33) - 1));
    }

    @Override
    public String toString() {
        String description = String.format("topic: %s, id: %d, data: %s\n", topic.getName(), id, Arrays.toString(data));
        return description;
    }
}
