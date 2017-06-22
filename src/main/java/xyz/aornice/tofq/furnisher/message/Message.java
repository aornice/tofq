package xyz.aornice.tofq.furnisher.message;

import io.netty.buffer.ByteBuf;
import xyz.aornice.tofq.furnisher.message.payload.Payload;
import xyz.aornice.tofq.furnisher.util.Recyclable;

public interface Message extends Recyclable {

    Operation getOp();

    Payload getPayload();

    void write(ByteBuf buf);
}
