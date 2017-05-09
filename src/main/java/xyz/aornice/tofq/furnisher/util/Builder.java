package xyz.aornice.tofq.furnisher.util;

import io.netty.buffer.ByteBuf;

public interface Builder<E extends Recyclable> {

    E build();

    E build(ByteBuf in) throws Exception;
}
