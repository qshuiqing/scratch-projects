package idv.projects.jdk.java.juc;

import java.util.concurrent.Semaphore;

/**
 * @author shaoq 2021/3/3 20:35
 */
public class QSemaphore {

    public static void main(String[] args) {
//        Semaphore s = new Semaphore(2);
//        Semaphore s = new Semaphore(2, true);
        Semaphore s = new Semaphore(1);

        new Thread(() -> {
            try {

                s.acquire();

                System.out.println("T1 running");
                Thread.sleep(200);
                System.out.println("T1 running");

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                s.release();
            }

        }).start();

        new Thread(() -> {
            try {

                s.acquire();

                System.out.println("T2 running");
                Thread.sleep(200);
                System.out.println("T2 running");

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                s.release();
            }

        }).start();

    }

}
