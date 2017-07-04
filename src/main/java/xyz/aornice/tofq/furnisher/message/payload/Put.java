package xyz.aornice.tofq.furnisher.message.payload;

import io.netty.buffer.ByteBuf;

public interface Put extends Payload {

    String getTopic();

    int getSeq();

    ByteBuf getData();

}
