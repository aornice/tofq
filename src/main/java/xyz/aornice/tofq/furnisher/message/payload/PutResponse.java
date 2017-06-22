package xyz.aornice.tofq.furnisher.message.payload;

public interface PutResponse extends Payload {

    String getTopic();

    int getSeq();

}
