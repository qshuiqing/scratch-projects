package idv.projects.jdk.java.juc.locks;

import sun.misc.Unsafe;

import java.util.concurrent.locks.LockSupport;

/**
 * @author shaoq 2021/3/4 13:46
 */
public class QAbstractQueuedSynchronizer {

    static final class Node {

        static final Node EXCLUSIVE = null; // 标记结点以独占模式等待

        // 1 - 取消
        // -1 - 后继需要唤醒
        // -2 - 线程等待条件
        // -3 - 传播？
        volatile int waitStatus;

        volatile Node prev; // 前驱

        volatile Node next; // 后继

        volatile Thread thread;

        Node nextWaiter;

        final Node predecessor() {
            Node p = prev;
            if (p == null) {
                throw new NullPointerException();
            } else {
                return p;
            }
        }

        Node() {
            // 用于创建初始结点 - 头结点
        }

        Node(Thread thread, Node mode) {
            this.thread = thread;
            this.nextWaiter = mode;
        }
    }

    private transient volatile Node head; // 指向头结点

    private transient volatile Node tail; // 指向尾结点

    private Node enq(final Node node) {
        // 入双端队列
        for (; ; ) {
            Node t = tail;
            if (t == null) {
                if (compareAndSetHead(new Node())) {
                    // 空链表，以 CAS 操作建立头结点，保证线程安全
                    tail = head;
                }
            } else {
                node.prev = t;
                if (compareAndSetTail(t, node)) {
                    t.next = node;
                    return t;
                }
            }
        }
    }

    private Node addWaiter(Node mode) {
        Node node = new Node(Thread.currentThread(), mode); //
        Node pred = tail;
        if (pred != null) { // 尾指针不为空，直接入队
            node.prev = pred;
            if (compareAndSetTail(pred, node)) { // 以 CAS 操作保证线程安全，尾部插入，尾指针后移
                pred.next = node;
                return node;
            }
        } // 尾指针为空，链表为空链，直接入队
        enq(node);
        return node;
    }

    private void unparkSuccessor(Node node) { // 如果需要，唤醒结点后继线程
        int ws = node.waitStatus;
        if (ws < 0) {
            compareAndSetWaitStatus(node, ws, 0);
        }
        Node s = node.next;
        if (s == null || s.waitStatus > 0) { // 遍历找寻离头结点最近的可被唤醒（waitStatus<=0）的结点
            s = null;
            for (Node t = tail; t != null && t != node; t = t.prev) {
                if (t.waitStatus <= 0) {
                    s = t;
                }
            }
        }
        if (s != null) { // 唤醒结点 s 中的线程
            LockSupport.unpark(s.thread);
        }
    }


    private volatile int state;

    protected final int getState() {
        return state;
    }

    protected final void setState(int state) {
        this.state = state;
    }

    protected final boolean compareAndSetState(int expect, int update) {
        return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
    }

    protected boolean tryRelease(int arg) {
        throw new UnsupportedOperationException();
    }

    public final void acquire(int arg) {
        if (!tryAcquire(arg) && acquireQueued(addWaiter(Node.EXCLUSIVE), arg)) {
            // 尝试获取锁失败
            //
            selfInterrupt();
        }
    }

    public final boolean release(int arg) {
        if (tryRelease(arg)) { // 释放锁
            Node h = this.head;
            if (h != null && h.waitStatus != 0) { //
                unparkSuccessor(h);
            }
            return true;
        }
        return false;
    }

    private boolean acquireQueued(Node addWaiter, int arg) {
        return false;
    }


    static void selfInterrupt() {
        Thread.currentThread().interrupt();
    }

    protected boolean tryAcquire(int arg) {
        throw new UnsupportedOperationException();
    }

    public final boolean hasQueuedPredecessors() {
        //TODO 是否已有等待
        return false;
    }


    private transient Thread exclusiveOwnerThread;

    protected final void setExclusiveOwnerThread(Thread thread) {
        this.exclusiveOwnerThread = thread;
    }

    protected final Thread getExclusiveOwnerThread() {
        return exclusiveOwnerThread;
    }

    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long stateOffset;
    private static final long headOffset;
    private static final long tailOffset;
    private static final long waitStatusOffset;


    static {
        try {
            stateOffset = unsafe.objectFieldOffset(QAbstractQueuedSynchronizer.class.getDeclaredField("state"));
            headOffset = unsafe.objectFieldOffset(QAbstractQueuedSynchronizer.class.getDeclaredField("head"));
            tailOffset = unsafe.objectFieldOffset(QAbstractQueuedSynchronizer.class.getDeclaredField("tail"));
            waitStatusOffset = unsafe.objectFieldOffset(Node.class.getDeclaredField("waitStatus"));
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }

    private final boolean compareAndSetHead(Node update) {
        return unsafe.compareAndSwapObject(this, headOffset, null, update);
    }

    private final boolean compareAndSetTail(Node expect, Node update) {
        return unsafe.compareAndSwapObject(this, tailOffset, expect, update);
    }

    private static final boolean compareAndSetWaitStatus(Node node, int expect, int update) {
        return unsafe.compareAndSwapInt(node, waitStatusOffset,
                expect, update);
    }

}
