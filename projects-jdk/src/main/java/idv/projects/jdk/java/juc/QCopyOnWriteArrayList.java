package idv.projects.jdk.java.juc;

import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ArrayList 的线程安全变体，所有的可变操作（add、set）都是通过对原有数组进行复制来实现的。
 *
 * @author shaoq 2021/3/4 12:04
 */
public class QCopyOnWriteArrayList<E> {

    final transient ReentrantLock lock = new ReentrantLock();

    private transient volatile Object[] array;

    final Object[] getArray() {
        return array;
    }

    final void setArray(Object[] a) {
        array = a;
    }

    public void add(int index, E element) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] elements = getArray();
            int len = elements.length;


            Object[] newElements;
            int numMoved = len - index;
            if (numMoved == 0) { // 尾部插入无需移动元素
                newElements = Arrays.copyOf(elements, len + 1);
            } else { // 非尾部插入需移动元素,index 分界移动两次
                newElements = new Object[len + 1];
                System.arraycopy(elements, 0, newElements, 0, index); // 移动前部分
                System.arraycopy(elements, index, newElements, index + 1, numMoved); // 移动后部分
            }
            newElements[index] = element;
            setArray(newElements);
        } finally {
            lock.unlock();
        }
    }

}
