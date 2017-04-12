package utils;

import com.google.common.base.Stopwatch;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by cat on 2017/4/11.
 */
public class FileLocatorTest {

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

    public static void main(String[] args){
        FileLocatorTest test = new FileLocatorTest();
        test.simpleModuloTime(8);
        System.out.println();
        test.newModuloTime(8);
    }

}
