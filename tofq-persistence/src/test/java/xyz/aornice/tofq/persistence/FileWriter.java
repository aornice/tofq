package xyz.aornice.tofq.persistence;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Aornice team
 * Created by drfish on 10/09/2017.
 */
public class FileWriter {
    private MappedFile mappedFile;
    private AtomicInteger position;

    public FileWriter(String fileName) {
        position = new AtomicInteger(0);
        try {
            mappedFile = new MappedFile(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(byte[] bytes) {
        mappedFile.write(bytes, position.get(), bytes.length);
        position.getAndAdd(bytes.length);
    }

    public static void main(String[] args) {
        FileWriter fileWriter = new FileWriter("test.tofq");
        String s = "tofq test";
        for (int i = 0; i < 1000; i++) {
            fileWriter.write(s.getBytes());
        }
    }
}
