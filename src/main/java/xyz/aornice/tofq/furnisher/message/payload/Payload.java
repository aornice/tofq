package xyz.aornice.tofq.furnisher.message.payload;

import io.netty.buffer.ByteBuf;
import xyz.aornice.tofq.furnisher.util.Recyclable;

public interface Payload extends Recyclable{

    void write(ByteBuf out);
}
