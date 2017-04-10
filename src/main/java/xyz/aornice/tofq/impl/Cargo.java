package xyz.aornice.tofq.impl;

/**
 * Created by robin on 10/04/2017.
 */
public class Cargo {
    private String topic;
    private long id;
    private byte[] data;


    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
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
