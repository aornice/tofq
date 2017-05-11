package xyz.aornice.tofq.furnisher.message.payload;

import io.netty.buffer.ByteBuf;
import xyz.aornice.tofq.furnisher.util.Builder;

public interface PayloadBuilder<E extends Payload> extends Builder<E> {

    E build(ByteBuf in) throws Exception;

}
