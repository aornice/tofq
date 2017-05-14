package xyz.aornice.tofq.network.serialize;

/**
 * Created by drfish on 11/05/2017.
 */
public interface TofqSerializable {
    byte[] serialize(final Object obj) throws Exception;

    <T> T deserialize(final byte[] data, Class<T> clazz) throws Exception;
}
