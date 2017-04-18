package deposition;

import org.junit.Before;
import xyz.aornice.tofq.depostion.support.LocalDeposition;

/**
 * Created by robin on 18/04/2017.
 */
public class LocalDepositionTest {

    LocalDeposition deposition;

    @Before
    public void setUp() {
        deposition = (LocalDeposition) LocalDeposition.getInstance();
    }
}
