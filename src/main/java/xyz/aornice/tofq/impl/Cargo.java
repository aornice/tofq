package xyz.aornice.tofq.impl;

/**
 * Created by robin on 10/04/2017.
 */
public class Cargo {
    private Topic topic;
    private long id;
    private byte[] data;


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
}
