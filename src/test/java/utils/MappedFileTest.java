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
        MappedBytes mappedBytes = mappedFile.acquireBytes(100000);
        System.out.print(mappedBytes);
    }
}
