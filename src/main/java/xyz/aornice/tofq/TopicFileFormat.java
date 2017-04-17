package xyz.aornice.tofq;

/**
 * Created by robin on 16/04/2017.
 */
public class TopicFileFormat {

    public static class Header {
        public static final int SIZE_BYTE = 4;
        public static final long OFFSET_BYTE = 0;
    }

    public static class Offset {
        public static final int CAPABILITY = 10000;
        public static final int OFFSET_SIZE_BYTE = 8;
        public static final int SIZE_BYTE = CAPABILITY * OFFSET_SIZE_BYTE;
        public static final long OFFSET_BYTE = Header.OFFSET_BYTE + Header.SIZE_BYTE;
    }

    public static class Data {
        public static final long OFFSET_BYTE = Offset.OFFSET_BYTE + Offset.SIZE_BYTE;
    }
}
