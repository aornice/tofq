package xyz.aornice.tofq;


import java.text.SimpleDateFormat;

/**
 * Created by robin on 16/04/2017.
 */
public class TopicFileFormat {

    public static class Header {
        public static final int COUNT_BYTE = 4;
        public static final int ID_START_BYTE = 8;
        public static final long COUNT_OFFSET_BYTE = 0;
        public static final long ID_START_OFFSET_BYTE = 4;
        public static final int SIZE_BYTE = COUNT_BYTE + ID_START_BYTE;
        public static final long OFFSET_BYTE = 0;
    }

    public static class Offset {
        public static final int CAPABILITY_POW = 15;
        public static final int CAPABILITY = 1 << CAPABILITY_POW;
        public static final int OFFSET_SIZE_BYTE = 8;
        public static final int SIZE_BYTE = CAPABILITY * OFFSET_SIZE_BYTE;
        public static final long OFFSET_BYTE = Header.OFFSET_BYTE + Header.SIZE_BYTE;
    }

    public static class Data {
        public static final long OFFSET_BYTE = Offset.OFFSET_BYTE + Offset.SIZE_BYTE;
    }

    public static class FileName {
        public static final int DATE_LENGTH = 8;
        public static final int START_IND = 0;
        public static final String SUFFIX = ".tof";
        public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
    }
}
