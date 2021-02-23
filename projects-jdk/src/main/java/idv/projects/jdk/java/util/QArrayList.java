package idv.projects.jdk.java.util;

import java.util.Arrays;

/**
 * ArrayList 是 List 接口的数组实现，且支持元素为 null 。ArrayList 与 Vector 类似，差异在于 ArrayList 非线程安全。
 * <p>
 * 引用指向数组的起始位置，对于任意下标 i 可通过 LOC(i) = LOC(0) + sizeof(element) * i 得出，即存储时间复杂度为 O(1)。
 *
 * @author qshuiqing 2021-02-20 16:50
 */
public class QArrayList<E> {

    // 数组，用于存放元素
    transient Object[] elementData;

    // elementData 中元素个数
    private int size;

    public QArrayList(int initialCapacity) {
        // 实例化数组
        elementData = new Object[initialCapacity];
    }

    public E get(int index) {
        return (E) elementData[index];
    }

    public void add(int index, E element) {
        // 校验是否需要扩容
        ensureExplicitCapacity(size + 1);
        // index 及之后的元素后移 1 位
        System.arraycopy(elementData, index, elementData, index + 1,
                size - index);
        elementData[index] = element;
        size++;
    }

    private void ensureExplicitCapacity(int minCapacity) {
        if (minCapacity - elementData.length > 0) {
            // 数组容量不够，需扩容
            int oldCapacity = elementData.length;
            // 扩容 1.5 倍
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            // 数组无法动态增长，需重新申请 newCapacity 大小的空间，将原有数据移动到新数组
            elementData = Arrays.copyOf(elementData, newCapacity);
        }
    }

    public E remove(int index) {
        E oldValue = (E) elementData[index];

        // 校验删除的时候之后一个元素
        // 若不是最后一个元素，需将该元素之后的数据前移 1 位
        int numMoved = elementData.length - index - 1;
        if (numMoved > 0) {
            System.arraycopy(elementData, index + 1, elementData, index, numMoved);
        }

        // 最后一个元素置为 null， GC 工作时清理对应对对象
        elementData[--size] = null;

        return oldValue;
    }

    public static void main(String[] args) {
        QArrayList<Object> qa = new QArrayList<>(10);
        qa.add(0, new Object());
        qa.add(0, new Object());
        qa.add(0, new Object());
        qa.add(0, new Object());
        qa.add(0, new Object());
        qa.add(0, new Object());
        qa.add(0, new Object());
        qa.add(0, new Object());
        qa.add(0, new Object());
        qa.add(0, new Object());
        qa.add(0, new Object());
        qa.add(0, new Object());
        System.out.println(qa.elementData.length);

        for (int i = 0, len = qa.size; i < len; i++) {
            qa.remove(0);
        }
        System.out.println(qa.size);

        QArrayList.class.getClassLoader();

    }

}
