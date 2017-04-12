package impl;

import xyz.aornice.tofq.Topic;
import xyz.aornice.tofq.impl.LocalExtraction;

import static org.junit.Assert.*;

/**
 * Created by cat on 2017/4/12.
 */
public class LocalExtractionTest {
    public static void main(String[] args){
        LocalExtraction test = new LocalExtraction(new HarbourMock());
        assertEquals(0x03, test.read(new Topic("topic1", 100),1).getData()[0]);

        assertEquals(null, test.read(new Topic("topic100",100),1));

        assertEquals(4, test.read(new Topic("topic1", 100), 1023,1027).length);

        assertEquals(0x00, test.read(new Topic("topic1", 100), 1023,1027)[0].getData()[0]);

        assertEquals(1025, test.read(new Topic("topic2", 100), 1023,3000).length);

    }
}
