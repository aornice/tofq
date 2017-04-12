import xyz.aornice.tofq.Cargo;
import xyz.aornice.tofq.TofQueue;
import xyz.aornice.tofq.impl.FurnisherData;
import xyz.aornice.tofq.impl.LocalTofQueue;

import java.util.Scanner;

/**
 * Created by drfish on 09/04/2017.
 */
public class BasicQueueTest {
    public static void main(String[] args) {
        TofQueue queue = new LocalTofQueue();
        Scanner read = new Scanner(System.in);
        while (true) {
            System.out.println("input some messages");
            String message = read.nextLine();
            if (message.isEmpty()) {
                break;
            }
            queue.offer(new FurnisherData());
        }
        System.out.println("---- furnish stage end ----");
        System.out.println("---- start receipt from harbour ----");
        Cargo[] cargo = queue.elements();
        System.out.println(cargo);
    }
}
