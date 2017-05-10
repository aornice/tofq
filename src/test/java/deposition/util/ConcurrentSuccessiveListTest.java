package deposition.util;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.depostion.util.support.ConcurrentSuccessiveList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by robin on 17/04/2017.
 */
public class ConcurrentSuccessiveListTest {
    ConcurrentSuccessiveList<Cargo> list;
    ExecutorService pool = Executors.newCachedThreadPool();

    @Before
    public void setUp() {
        list = new ConcurrentSuccessiveList<>(8, 4);
    }

    @Test
    public void put() {
        Cargo cargoI = new Cargo();
        cargoI.setId(4);
        list.put(cargoI);
        assertEquals(1, list.size());

        for (int i = 20; i > 5; i--) {
            Cargo cargo = new Cargo();
            cargo.setId(i);
            list.put(cargo);
        }
        assertEquals(17, list.size());
    }

    @Test
    public void parallelPut() throws ExecutionException, InterruptedException {
        Future<?> f1 = pool.submit(() -> {
            for (int i = 5; i < 20; i += 2) {
                Cargo cargo = new Cargo();
                cargo.setId(i);
                list.put(cargo);
            }
        });
        Future<?> f2 = pool.submit(() -> {
            for (int i = 6; i < 20; i += 2) {
                Cargo cargo = new Cargo();
                cargo.setId(i);
                list.put(cargo);
            }
        });

        f1.get();
        f2.get();
        assertEquals(16, list.size());
    }

    @Test
    public void resize() {
        for (int i = 5; i < 20; i++) {
            Cargo cargo = new Cargo();
            cargo.setId(i);
            list.put(cargo);
        }

        for (int i = 21; i < 40; i++) {
            Cargo cargo = new Cargo();
            cargo.setId(i);
            list.put(cargo);
        }
        Cargo cargo = new Cargo();
        cargo.setId(20);
        list.put(cargo);

        assertEquals(36, list.size());
    }

    @Test
    public void takeAllSuccessive() {
        for (int i = 5; i < 40; i++) {
            Cargo cargo = new Cargo();
            cargo.setId(i);
            list.put(cargo);
        }
        assertEquals(0, list.successiveSize());

        for (int i = 41; i < 45; i++) {
            Cargo cargo = new Cargo();
            cargo.setId(i);
            list.put(cargo);
        }

        Cargo cargo = new Cargo();
        cargo.setId(4);
        list.put(cargo);

        assertEquals(41, list.size());

        assertEquals(36, list.successiveSize());
        List<Cargo> rst = new ArrayList<>();
        assertEquals(list.takeAllSuccessive(4, rst), true);
        for (int i = 4; i < 40; i++) assertEquals(rst.get(i - 4).getId(), i);
        assertEquals(5, list.size());
        assertEquals(0, list.successiveSize());
        Cargo cargo2 = new Cargo();
        cargo2.setId(40);
        list.put(cargo2);
        assertEquals(5, list.size());
        assertEquals(5, list.successiveSize());
        rst.clear();
        assertEquals(list.takeAllSuccessive(40, rst), true);
        for (int i = 0; i < 5; i++) assertEquals(rst.get(i).getId(), i + 40);
        assertEquals(0, list.size());
        assertEquals(0, list.successiveSize());
    }
}
