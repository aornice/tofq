package utils;

import xyz.aornice.tofq.harbour.MappedBytes;
import xyz.aornice.tofq.harbour.MappedFile;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by drfish on 12/04/2017.
 */
public class MappedFileTest {
    public static void main(String[] args) throws IOException, IllegalAccessException, InvocationTargetException {
        MappedFile mappedFile = MappedFile.getMappedFile("test.txt", 1024);
        MappedBytes mappedBytes = mappedFile.acquireBytes(10000);
        mappedBytes.writeLong(10, 12412312412L);
        mappedBytes.writeInt(20, 323);
        System.out.println(mappedBytes.readLong(10));
        String s = "hello world!";
        mappedBytes.writeBits(s, 100, 100+s.length());
        System.out.print(mappedBytes);
    }
}
