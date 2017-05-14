package xyz.aornice.tofq.network.codec;

import xyz.aornice.tofq.network.command.Command;

import java.nio.ByteBuffer;

/**
 * a codec can encode and decode a command message
 * Created by drfish on 09/05/2017.
 */
public interface Codec {
    Command decode(ByteBuffer byteBuffer) throws Exception;

    ByteBuffer encode(Command command) throws Exception;
}
