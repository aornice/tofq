package xyz.aornice.tofq.network.codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.aornice.tofq.network.command.Command;
import xyz.aornice.tofq.network.serialize.TofqSerializable;

import java.nio.ByteBuffer;

/**
 * Created by drfish on 11/05/2017.
 */
public class SampleCodec implements Codec {
    private static final Logger logger = LoggerFactory.getLogger(SampleCodec.class);
    private TofqSerializable tofqSerializable;

    public SampleCodec(TofqSerializable tofqSerializable) {
        this.tofqSerializable = tofqSerializable;
    }

    @Override
    public Command decode(ByteBuffer byteBuffer) throws Exception {
        int length = byteBuffer.limit();
        byte[] bodyData = new byte[length];
        byteBuffer.get(bodyData);
        logger.debug("decoded body: {}", bodyData);
        Command result = tofqSerializable.deserialize(bodyData, Command.class);
        return result;
    }

    @Override
    public ByteBuffer encode(Command command) throws Exception {
        int length = 4;
        byte[] bodyData = tofqSerializable.serialize(command);
        logger.debug("encoded body: {}", bodyData);
        length += bodyData.length;
        ByteBuffer result = ByteBuffer.allocate(length);
        result.putInt(length - 4);
        result.put(bodyData);
        result.flip();
        return result;
    }
}
