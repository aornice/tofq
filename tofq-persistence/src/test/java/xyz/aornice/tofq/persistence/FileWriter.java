package xyz.aornice.tofq.persistence;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Aornice team
 * Created by drfish on 10/09/2017.
 */
public class FileWriter {
    private MappedFile mappedFile;
    private AtomicInteger position;
    private AtomicInteger index;
    private String fileName;

    private static final String TMP_OUTPUT_FOLDER = "tmp/";
    private static final String TOFQ_FILE_SUFFIX = "tofq";
    private static final int FILE_SIZE = 1024 * 1024 * 1024;

    static {
        File file = new File(TMP_OUTPUT_FOLDER);
        file.mkdir();
    }

    public FileWriter(String fileName) {
        position = new AtomicInteger(0);
        index = new AtomicInteger(0);
        this.fileName = fileName;
        createNewMappedFile();
    }

    private static String generateFileName(String fileName, int index) {
        String fullFileName = fileName + index + "." + TOFQ_FILE_SUFFIX;
        if (!fullFileName.contains("/")) {
            fullFileName = TMP_OUTPUT_FOLDER + fullFileName;
        }
        return fullFileName;
    }

    private void createNewMappedFile() {
        String tofqFileName = generateFileName(fileName, index.get());
        try {
            mappedFile = new MappedFile(tofqFileName, FILE_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(byte[] bytes) {
        int prePosition = position.getAndAdd(bytes.length);
        if (position.get() > FILE_SIZE) {
            index.incrementAndGet();
            createNewMappedFile();
            position.set(0);
            prePosition = 0;
        }
        mappedFile.write(bytes, prePosition, bytes.length);
    }


    public static void main(String[] args) {
        FileWriter fileWriter = new FileWriter("test");
        String s = "tofq test";
        for (int i = 0; i < 400000000; i++) {
            fileWriter.write(s.getBytes());
        }
    }
}
