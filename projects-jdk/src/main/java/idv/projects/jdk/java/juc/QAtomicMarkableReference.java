package idv.projects.jdk.java.juc;

import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * @author shaoq 2021/3/3 12:47
 */
public class QAtomicMarkableReference {


    private static class Order {
        long sequence;
        long time;

        @Override
        public String toString() {
            return "Order{" +
                    "sequence=" + sequence +
                    ", time=" + time +
                    '}';
        }
    }


    static AtomicMarkableReference<Order> orderRef = new AtomicMarkableReference<>(new Order(), false);

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            new Thread(() -> {
                Order old = orderRef.getReference();

                Order o = new Order();
                o.sequence = old.sequence + 1;
                o.time = System.currentTimeMillis();

                orderRef.compareAndSet(old, o, false, true);

            }).start();
        }

        Thread.sleep(1000000);

        System.out.println(orderRef.getReference());

    }


}
