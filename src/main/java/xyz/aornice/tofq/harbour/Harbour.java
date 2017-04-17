package xyz.aornice.tofq.harbour;

import java.util.List;

/**
 * Created by drfish on 10/04/2017.
 */
public interface Harbour {
    byte[] get(String fileName, long offset);

    /**
     *
     * @param fileName
     * @param offsetFrom  start offset in byte, included
     * @param offsetTo    end offset in byte, not included
     * @return
     */
    byte[] get(String fileName, long offsetFrom, long offsetTo);

    void put(String fileName, byte[] data);

    void put(String fileName, byte[] data, long offset);

    void flush(String fileName);
}
