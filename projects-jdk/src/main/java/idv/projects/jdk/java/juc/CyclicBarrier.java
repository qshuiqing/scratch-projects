package idv.projects.jdk.java.juc;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 允许一组线程在该点等待一起执行。
 *
 * @author shaoq 2021/3/8 11:39
 */
public class CyclicBarrier {

    // 记录当前壁垒的状态
    // 打破壁垒或重置壁垒时会修改此值
    private static class Generation { //151
        boolean broken = false;
    }

    private ReentrantLock lock = new ReentrantLock(); // 156
    private final Condition trip = lock.newCondition(); //TODO Condition???? & signalAll()
    // 一组内的线程数，即多少线程后打破壁垒
    private final int parties;
    // 壁垒打破前执行
    private final Runnable barrierCommand;
    private Generation generation = new Generation();

    // 还需有多少线程才能打破壁垒
    private int count;

    private void nextGeneration() {
        trip.signalAll();
        count = parties;
        generation = new Generation();
    }

    private void breakBarrier() { // 189
        generation.broken = true;
        count = parties;
        trip.signalAll();
    }

    private int doAwait(boolean timed, long nanos) throws BrokenBarrierException, InterruptedException, TimeoutException { // 198
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            final Generation g = generation;

            // 壁垒已被打破
            if (g.broken) throw new BrokenBarrierException();

            if (Thread.interrupted()) {
                breakBarrier();
                throw new InterruptedException();
            }

            int index = --count;
            if (index == 0) {
                // 壁垒被打破
                boolean ranAction = false;
                try {
                    // 执行壁垒后值
                    final Runnable command = barrierCommand;
                    if (command != null) command.run();
                    ranAction = true;
                    nextGeneration();
                    return 0;
                } finally {
                    if (!ranAction) {
                        breakBarrier();
                    }
                }
            }

            for (; ; ) {
                try {
                    if (!timed) trip.await();
                    else if (nanos > 0L) nanos = trip.awaitNanos(nanos);
                } catch (InterruptedException ie) {
                    if (g == generation && !g.broken) {
                        breakBarrier();
                        throw ie;
                    } else Thread.currentThread().interrupt();
                }

                if (g.broken) throw new BrokenBarrierException();

                if (g != generation) return index;

                if (timed && nanos <= 0L) {
                    breakBarrier();
                    throw new TimeoutException();
                }
            }

        } finally {
            lock.unlock();
        }
    }

    public CyclicBarrier(int parties, Runnable barrierAction) { // 277
        if (parties < 0) throw new IllegalArgumentException();
        this.parties = parties;
        this.count = parties;
        this.barrierCommand = barrierAction;
    }

    public CyclicBarrier(int parties) { // 293
        this(parties, null);
    }


    // 等待
    public int await() throws BrokenBarrierException, InterruptedException, TimeoutException { // 368
        return doAwait(false, 0L);
    }


}
