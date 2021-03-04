package idv.projects.jdk.java.juc.locks;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author shaoq 2021/3/4 13:15
 */
public class QReentrantLock {

    abstract static class Sync extends QAbstractQueuedSynchronizer {
        // 锁的实质是所有线程都对 state 设值
        // 那个线程能将 state 从 0 改为其他的值，也就意味抢到了锁
        // 只有当该线程释放锁，重新将 state 设为 0 时，其他线程才能抢锁
        // 当同一个线程多次请求锁
        // 公平锁排队等待，非公平锁直接 state 加值

        abstract void lock();

        final boolean nonfairTryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) { // 没有线程尝试获取该锁
                if (compareAndSetState(0, acquires)) {
                    // 基于 CAS 操作
                    // 记录持有锁的线程
                    setExclusiveOwnerThread(current);
                    return true;
                }
            } else if (current == getExclusiveOwnerThread()) {
                // 是当前线程持有锁，可重入
                int nextc = c + acquires;
                if (nextc < 0) { // 数值溢出，超过可表示最大值
                    throw new Error("Maximum lock count exceeded");
                }
                setState(nextc);
                return true;
            }
            return false;
        }

        @Override
        protected final boolean tryRelease(int releases) {
            int c = getState() - releases;
            if (Thread.currentThread() != getExclusiveOwnerThread()) {
                throw new IllegalMonitorStateException();
            }
            boolean free = false;
            if (c == 0) { // 持有锁的线程释放了锁，若不为零说明线程多次持有该锁
                free = true;
                setExclusiveOwnerThread(null);
            }
            setState(c);
            return free;
        }

    }

    static final class NonfairSync extends Sync { // 非公平锁，只要锁没有被持有，就可以去抢占，对等待队列中请求锁的线程不公平

        @Override
        final void lock() {
            if (compareAndSetState(0, 1)) { // 基于 CAS 操作，若 state = 0，则设置为 1 并返回成功，否则失败
                // 记录持有锁的线程
                setExclusiveOwnerThread(Thread.currentThread());
            } else {
                acquire(1);
            }
        }

        @Override
        protected final boolean tryAcquire(int acquires) {
            return nonfairTryAcquire(acquires);
        }
    }

    static final class FairSync extends Sync { // 公平锁

        @Override
        final void lock() {
            acquire(1);
        }

        @Override
        protected final boolean tryAcquire(int acquires) {
            Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                if (!hasQueuedPredecessors() && compareAndSetState(0, acquires)) {
                    //
                    // 基于 CAS 抢锁
                    setExclusiveOwnerThread(current);
                    return true;
                }
            } else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0) {
                    throw new Error("Maximum lock count exceeded");
                }
                setState(nextc);
                return true;
            }
            return false;
        }

    }

    final transient ReentrantLock lock = new ReentrantLock(false);

}
