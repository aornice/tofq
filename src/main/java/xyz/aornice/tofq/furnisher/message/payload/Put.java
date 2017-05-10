package xyz.aornice.tofq.furnisher.message.payload;

import io.netty.buffer.ByteBuf;
import xyz.aornice.tofq.furnisher.util.Recyclable;

public interface Put extends Recyclable{

    String getTopic();

    int getSeq();

    ByteBuf getData();

}
