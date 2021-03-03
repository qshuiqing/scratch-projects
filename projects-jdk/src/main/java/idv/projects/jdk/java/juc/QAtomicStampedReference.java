package idv.projects.jdk.java.juc;

import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * @author shaoq 2021/3/3 12:40
 */
public class QAtomicStampedReference {

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


    static AtomicStampedReference<Order> orderRef = new AtomicStampedReference<>(new Order(), 0);

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            new Thread(() -> {
                Order old = orderRef.getReference();
                int stamp = orderRef.getStamp();


                Order o = new Order();
                o.sequence = old.sequence + 1;
                o.time = System.currentTimeMillis();

                orderRef.compareAndSet(old, o, stamp, stamp + 1);

            }).start();
        }


        Thread.sleep(10000);

        System.out.println(orderRef.getReference());
    }

}
