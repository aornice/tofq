package utils;

import com.google.common.base.Stopwatch;
import xyz.aornice.tofq.utils.FileLocater;
import xyz.aornice.tofq.utils.impl.LocalFileLocator;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Created by cat on 2017/4/11.
 */
public class FileLocatorTest {

    private FileLocater fileLocater = LocalFileLocator.newInstance();

    private void simpleModuloTime(long n){
        long[] results = new long[1];
        Stopwatch stopwatch = Stopwatch.createStarted();
        for (long i=1;i<1e8;i++){
            results[0] = i%n;
        }
        stopwatch.stop();
        System.out.println("time is "+stopwatch.elapsed(TimeUnit.MILLISECONDS));

    }

    private void newModuloTime(long n){
        long[] results = new long[1];
        Stopwatch stopwatch = Stopwatch.createStarted();
        for (int i=1;i<1e8;i++){
            results[0] = i & (n-1);
        }
        stopwatch.stop();
        System.out.println("time is "+stopwatch.elapsed(TimeUnit.MILLISECONDS));

    }

    private void testFileList(){
        try {
            Field field= LocalFileLocator.class.getDeclaredField("topicFileMap");
            field.setAccessible(true);
            Map<String, ArrayList<String>> topicFileMap = (Map<String,ArrayList<String>>)field.get(LocalFileLocator.class);

            for (Map.Entry<String, ArrayList<String>> entry: topicFileMap.entrySet()){
                System.out.println(entry.getKey()+":");
                for (String fileName: entry.getValue()){
                    System.out.println("\t"+fileName);
                }
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args){
        FileLocatorTest test = new FileLocatorTest();
//        test.simpleModuloTime(8);
//        System.out.println();
//        test.newModuloTime(8);
    }

}
