package xyz.aornice.tofq.harbour;

import java.util.List;

/**
 * Created by drfish on 10/04/2017.
 */
public interface Harbour {
    /**
     * get bytes array from file with offset between argument {@code offsetFrom} and argument {@code offsetTo}
     *
     * @param fileName   file name
     * @param offsetFrom start offset in byte, included
     * @param offsetTo   end offset in byte, excluded
     * @return byte array needed
     */
    byte[] get(String fileName, long offsetFrom, long offsetTo);

    /**
     * get long list from file starting form {@code offset} with count of {@code count}
     *
     * @param fileName file name
     * @param offset   offset from the file in byte, included
     * @param count    count of longs needed
     * @return long list
     */
    List<Long> getLongs(String fileName, long offset, long count);

    /**
     * get a long from file at {@code offset}
     *
     * @param fileName file name
     * @param offset   offset in the file
     * @return long
     */
    long getLong(String fileName, long offset);

    /**
     * get an int from file at {@code offset}
     *
     * @param fileName file name
     * @param offset   offset in the file
     * @return int
     */
    int getInt(String fileName, long offset);

    /**
     * put bytes in a byte array into a file from the offset of {@code offset}
     *
     * @param fileName file name
     * @param data     bytes needed being written
     * @param offset   writing position in the file
     */
    void put(String fileName, byte[] data, long offset);

    /**
     * put a long into a file at offset of {@code offset}
     *
     * @param fileName file name
     * @param val      long value needed being written
     * @param offset   writing position in the file
     */
    void put(String fileName, long val, long offset);

    /**
     * put an int into a file at offset of {@code offset}
     *
     * @param fileName file name
     * @param val      int value needed being written
     * @param offset   writing position in the file
     */
    void put(String fileName, int val, long offset);

    /**
     * flush memory data into disk
     *
     * @param fileName file name
     */
    void flush(String fileName);

    /**
     * create a file
     *
     * @param fileName file name
     * @return whether created successfully
     */
    boolean create(String fileName);

    /**
     * remove the file of cargoes or folder of topic
     * this method can cache the removed file or folder for a while
     *
     * @param fileName file name
     * @return <code>true</code> if and only if the removing is succeeded;
     * <code>false</code> otherwise
     */
    boolean remove(String fileName);
}
