package idv.projects.jdk.java.util;

import java.util.LinkedList;

/**
 * LinkedList 是 List 接口的双向链表实现，且支持元素为 null 。非线程安全。
 * <p>
 * 基于下标对 LinkedList 的操作都需要遍历链表。
 *
 * @author qshuiqing 2021-02-22 13:49
 */
public class QLinkedList<E> {


    private int size;// 元素个数

    transient Node<E> first; // 指向双向链表头结点

    transient Node<E> last; // 指向双向链表尾结点

    private static class Node<E> { // 链表结点
        E item; // 元素
        Node<E> prev; // 指向前驱结点
        Node<E> next; // 指向后继结点

        public Node(Node<E> prev, E element, Node<E> next) {
            this.item = element;
            this.prev = prev;
            this.next = next;
        }
    }

    Node<E> node(int index) {
        if (index < (size >> 1)) {
            // index 小于元素个数的一半，接近头结点，从头结点遍历
            Node<E> x = first;
            for (int i = 0; i < index; i++) {
                x = first.next;
            }
            return x;
        } else {
            // index 接近尾结点，从尾结点遍历
            Node<E> x = last;
            for (int i = size - 1; i > index; i--) {
                x = last.prev;
            }
            return x;
        }
    }

    public void add(int index, E e) { // 指定下标元素插入
        if (index == size) { // 尾结点后插入
            final Node<E> l = last;
            final Node<E> newNode = new Node<>(l, e, null);
            last = newNode; // 指向新的尾结点
            if (l == null) {
                first = newNode; // 首次添加元素，指向头结点
            } else {
                l.next = newNode;
            }
        } else { // 定位 index 结点位置，修改相应结点前驱后继
            Node<E> succ = node(index);
            final Node<E> prev = succ.prev;
            final Node<E> newNode = new Node<>(prev, e, succ);
            succ.prev = newNode;
            if (prev == null) {
                first = newNode; // 下标 0 插入，指向新的头结点
            } else {
                prev.next = newNode;
            }
        }
        size++;
    }

}
