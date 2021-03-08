package idv.projects.interview.thread;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * @author shaoq 2021/3/8 11:27
 */
public class CyclicBarrierTest {

    public static final CyclicBarrier barrier = new CyclicBarrier(10, () -> System.out.println(System.currentTimeMillis()));

    public static void main(String[] args) {

        for (int i = 0; i < 100; i++) {
            int finalI = i;
            new Thread(() -> {
                try {
                    Thread.sleep(1000 * (finalI / 10));
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
                System.out.println("let's go!");
            }, "Thread" + i).start();
        }

    }

}
