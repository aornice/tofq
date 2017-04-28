package impl;

import xyz.aornice.tofq.utils.ExtractionHelper;
import xyz.aornice.tofq.utils.impl.LocalExtractionHelper;

import static org.junit.Assert.assertEquals;

/**
 * Created by shen on 2017/4/25.
 */
public class ExtractionHelperTest {
    public static void main(String[] args){
        ExtractionHelper test = LocalExtractionHelper.getInstance();
        assertEquals(1024, test.nextStartIndex(1));
        assertEquals(1024, test.nextStartIndex(0));
        assertEquals(1024, test.nextStartIndex(1023));
        assertEquals(2048, test.nextStartIndex(1024));
    }
}
