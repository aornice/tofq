package xyz.aornice.tofq.utils.impl;


import xyz.aornice.tofq.Setting;
import xyz.aornice.tofq.TopicFileFormat.FileName;

import java.util.Comparator;
import java.util.Date;

/**
 * Created by shen on 2017/4/17.
 */
public class CargoFileUtil {
    // date length in file name
    private static final String FILE_SEPERATOR = System.getProperty("file.separator");

    private static final Comparator<String> fileSortComparator = (String file1, String file2) -> {
        // first compare the date
        for (int i = 0; i < FileName.DATE_LENGTH; i++) {
            if (file1.charAt(i) != file2.charAt(i)) {
                return file1.charAt(i) - file2.charAt(i);
            }
        }
        // smaller if in the same date and file name is shorter
        if (file1.length() != file2.length()) {
            return file1.length() - file2.length();
        }
        return file1.compareTo(file2);
    };

    private static final Comparator<String> dateComparator = (String str1, String str2) -> {
        int diff;
        for (int i = 0; i < FileName.DATE_LENGTH; i++) {
            diff = str1.charAt(i) - str2.charAt(i);
            if (diff != 0) {
                return diff > 0 ? 1 : -1;
            }
        }
        return 0;
    };

    public static int fileCompareDateStr(String fileName, String date) {
        return dateComparator.compare(fileName, date);
    }


    public static Comparator<String> getFileSortComparator() {
        return fileSortComparator;
    }

    public static String getFileSeperator() {
        return FILE_SEPERATOR;
    }

    public static String getTopicRoot() {
        return Setting.TOPIC_ROOT;
    }

    public static String filePath(String topicPath, String fileName) {
        return topicPath + FILE_SEPERATOR + fileName;
    }

    public static String dateStr(Date date) {
        return FileName.DATE_FORMAT.format(date);
    }
}
