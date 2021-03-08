package idv.projects.interview.thread;

import java.util.concurrent.Semaphore;

/**
 * 三线程，一线程输出 a，二线程输出 b，三线程输出 c
 *
 * @author shaoq 2021/3/8 10:33
 */
public class ABCOut_Semaphore {

    public static void main(String[] args) {

        Semaphore ao = new Semaphore(0); // 输出 a
        Semaphore bo = new Semaphore(0); // 输出 b
        Semaphore co = new Semaphore(0); // 输出 c

        new Thread(() -> {
            for (int i = 0; ; i++) {
                if (i != 0) {
                    try {
                        co.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("a");
                ao.release();
            }
        }).start();

        new Thread(() -> {
            for (; ; ) {
                try {
                    ao.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("b");
                bo.release();
            }
        }).start();

        new Thread(() -> {
            for (; ; ) {
                try {
                    bo.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("c");
                co.release();
            }
        }).start();

        Thread.currentThread().setDaemon(true);

    }

}
