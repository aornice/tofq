package xyz.aornice.tofq.harbour;

/**
 * Created by drfish on 10/04/2017.
 */
public interface Harbour {
    byte[] get(String fileName, long offset);

    void put(String fileName, byte[] data);
}
