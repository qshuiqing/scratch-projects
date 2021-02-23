package idv.projects.jdk.java.util;

/**
 * LinkedList 是 List 接口的双向链表实现，且支持元素为 null 。非线程安全。
 * <p>
 * 基于下标对 LinkedList 的操作都需要遍历链表。
 *
 * @author qshuiqing 2021-02-22 13:49
 */
public class QLinkedList<E> {

    // 指向双向链表首部元素
    transient Node<E> first;

    // 指向双向链表尾部元素
    transient Node<E> last;

    // 链表结点
    private static class Node<E> {
        E item;
        Node prev;
        Node next;

        public Node(Node prev, E item, Node next) {
            this.item = item;
            this.prev = prev;
            this.next = next;
        }
    }

}
