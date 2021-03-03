package idv.projects.jdk.java.util;

/**
 * HashMap 是 Map 接口的实现，允许键值为 null。 HashMap 与 Hashtable 大体一致，不同之处在于 HashMap 为非线程安全类且允许键值为 null 。
 *
 * @author qshuiqing 2021/3/1 11:54 上午
 */
public class QHashMap<K, V> {

    transient Node<K, V>[] table; // 桶

    transient int size; // 元素个数

    // java 中 int 类型占 32 位，可表示的最大整数位 2^31 - 1（最高位符号位）
    // 可表示最大 2 的幂次方位为 2^30（1 << 30）
    static final int MAXIMUM_CAPACITY = 1 << 30;

    int threshold; // 扩容阈值

    static final float DEFAULT_LOAD_FACTOR = 0.75f; // 装填因子

    static final int TREEIFY_THRESHOLD = 8; // 树化阈值

    static final int MIN_TREEIFY_CAPACITY = 64; // 转化红黑树最小桶数

    public QHashMap(int initialCapacity) {
        int cap = tableSizeFor(initialCapacity);
        table = new Node[cap];
        threshold = (int) (cap * DEFAULT_LOAD_FACTOR);
    }

    // 获取不小于 cap 值的最小 2 的幂次方
    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n > MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    static final int hash(Object key) {
        int h;
        // 若 key 为 null，元素存放到 table[0] 中
        return key == null ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    public V put(K key, V value) {
        int i; // key 映射到的桶的编号
        Node<K, V> p; // i 号桶首个元素
        int hash;
        if ((p = table[i = (table.length - 1) & (hash = hash(key))]) == null) {
            table[i] = new Node<>(hash, key, value, null); // 桶空，直接插入
        } else {
            Node<K, V> e; // 指向按 hash,key 匹配到的元素，如果未匹配到则为 null
            K k;
            if (p.hash == hash && // key 比较操作比 hash 比较操作耗时
                    ((k = p.key) == key || (key != null && key.equals(k)))) {
                e = p; // 匹配到首个元素
            } else if (p instanceof TreeNode) { // 红黑树结点，使用红黑树算法遍历
                e = ((TreeNode<K, V>) p).putTreeVal(this, table, hash, key, value);
            } else { // 首元素未匹配且非红黑树结点，遍历链表
                for (int binCount = 0; ; ++binCount) {
                    if ((e = p.next) == null) { // 未匹配，尾部插入
                        p.next = new Node<>(hash, key, value, null);
                        if (binCount >= TREEIFY_THRESHOLD - 1) { // 桶中元素（包括首元素） >= 8 转红黑树
                            treeifyBin(table, hash);
                        }
                        break;
                    }
                    if (e.hash == hash && ((k = p.key) == key || (key != null && key.equals(k)))) {
                        // 匹配，跳出循环
                        break;
                    }
                    p = e; // 指针后移
                }
            }
            if (e != null) { // 按 hash，key 匹配到元素
                V oldValue = e.value;
                e.value = value;
                return oldValue;
            }
        }
        if (++size > threshold) { // 超过阈值，扩容
            resize();
        }
        return null;
    }

    public V get(Object key) {
        Node<K, V> first, e;
        K k;
        int hash;
        if ((first = table[(table.length - 1) & (hash = hash(key))]) != null) {
            if (first.hash == hash &&
                    ((k = first.key) == key || (key != null && key.equals(k)))) { // 首元素匹配
                return first.value;
            }
            if ((e = first.next) != null) {
                if (first instanceof TreeNode) { // 红黑树遍历匹配
                    Node<K, V> h = ((TreeNode<K, V>) first).getTreeNode(hash, key);
                    return h == null ? null : h.value;
                }
                do { // 链表遍历匹配
                    if (e.hash == hash &&
                            ((k = e.key) == key || (key != null && key.equals(k)))) {
                        return e.value;
                    }
                } while ((e = e.next) != null);
            }
        }
        // 桶空
        return null;
    }

    public V remove(Object key) {
        Node<K, V> p; // 指向第一个元素或匹配到的元素的前一个元素
        int index; // key 映射到的桶编号
        int hash;
        if ((p = table[index = (table.length - 1) & (hash = hash(key))]) != null) {
            Node<K, V> node = null, e;
            K k;
            if (p.hash == hash &&
                    ((k = p.key) == key || (key != null && key.equals(k)))) { // 首元素匹配
                node = p;
            } else if ((e = p.next) != null) {
                if (p instanceof TreeNode) { // 红黑树遍历
                    node = ((TreeNode<K, V>) p).getTreeNode(hash, key);
                } else { // 链表遍历
                    do {
                        if (e.hash == hash &&
                                ((k = e.key) == key ||
                                        (key != null && key.equals(k)))) {
                            node = e;
                            break;
                        }
                        p = e;
                    } while ((e = e.next) != null);
                }
            }
            if (node != null) { // 匹配到元素，删除元素
                if (node instanceof TreeNode) { // 红黑树删除元素，若 < 6 元素链表化
                    ((TreeNode<K, V>) node).removeTreeNode(this, table);
                } else if (node == p) { // 首元素删除
                    table[index] = node.next;
                } else { // 非首元素删除，链接元素
                    p.next = node.next;
                }
                --size;
                return node.value;
            }
        }
        // 空桶
        return null;
    }


    final void resize() {
        Node<K, V>[] oldTab = table;
        int oldCap = oldTab.length;

        int newCap = table.length << 1;// x2
        threshold = threshold << 1; // x2

        table = new Node[newCap]; // 扩容新数组
        for (int j = 0; j < oldCap; j++) { // 遍历所有元素， rehash
            Node<K, V> e;
            if ((e = oldTab[j]) != null) { // 非空桶
                oldTab[j] = null;
                if (e.next == null) { // 桶中只有一个元素直接 rehash 即可
                    table[e.hash & (newCap - 1)] = e;
                } else if (e instanceof TreeNode) { // rehash 红黑树
                    ((TreeNode<K, V>) e).split(this, table, j, oldCap);
                } else {
                    // 桶中有多个元素，遍历 rehash
                    // rehash 后，元素只有两种可能：1-映射到 j 内；2-映射到 j + oldCap 内
                    Node<K, V> loHead = null; // 指向映射后落到 j 的第一个结点
                    Node<K, V> loTail = null; // 指向映射后落到 j 的尾结点
                    Node<K, V> hiHead = null; // 指向映射后落到 j + newCap 的第一个结点
                    Node<K, V> hiTail = null; // 指向映射后落到 j + newCap 的尾结点
                    Node<K, V> next; // 指向当前操作结点的下一个
                    do { // 开始遍历所有元素并 rehash
                        next = e.next;
                        if ((e.hash & oldCap) == 0) {
                            // hash 增加 1 位与 newCap - 1 取模落在 j 上
                            // 通过尾插法链接
                            if (loTail == null) {
                                loHead = e; // 首个元素
                            } else {
                                loHead.next = e;
                            }
                            loTail = e;
                        } else { // hash 增加 1 位与 newCap - 1 取模落在 oldCap + j 上
                            if (hiTail == null) {
                                hiHead = e; // 首个元素
                            } else {
                                hiTail.next = e;
                            }
                            hiTail = e;
                        }
                    } while ((e = next) != null);
                    if (loTail != null) {
                        loTail.next = null;
                        table[j] = loHead;
                    }
                    if (hiTail != null) {
                        hiTail.next = null;
                        table[j + oldCap] = hiHead;
                    }
                }
            }
        }
    }

    final void treeifyBin(Node<K, V>[] tab, int hash) {
        if (tab.length < MIN_TREEIFY_CAPACITY) { // 若桶数量 < 64 直接扩容而不用转红黑树
            resize();
        }
        Node<K, V> e = tab[(tab.length - 1) & hash];
        TreeNode<K, V> hd = null, tl = null;
        do { // 将普通结点转化为红黑树结点并使用双链表链接
            TreeNode<K, V> p = new TreeNode<>(hash, e.key, e.value, null); // 转换成红黑树结点
            if (tl == null) { // 首次循环指向头
                hd = p;
            } else { // 添加结点，双向链表
                p.prev = tl;
                tl.next = p;
            }
            tl = p;
        } while ((e = e.next) != null);
        hd.treeify(tab); // 双向链表转红黑树
    }


    static class Node<K, V> { // 普通结点
        final int hash; // hash 值用于匹配与 rehash
        final K key; // hash 值一样按 key 匹配
        V value;
        Node<K, V> next; // 指向后继

        public Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    static final class TreeNode<K, V> extends Node<K, V> { // 红黑树结点
        TreeNode<K, V> parent;
        TreeNode<K, V> left; // 左孩子
        TreeNode<K, V> right; // 右孩子
        TreeNode<K, V> prev; // 前驱
        boolean red; // 红黑标记

        public TreeNode(int hash, K key, V value, Node<K, V> next) {
            super(hash, key, value, next);
        }

        final TreeNode<K, V> putTreeVal(QHashMap<K, V> map, Node<K, V>[] tab,
                                        int h, K k, V v) {
            //TODO 红黑树检索，插入，更新
            return null;
        }

        final void treeify(Node<K, V>[] tab) {
            //TODO 树化
        }

        final TreeNode<K, V> getTreeNode(int h, Object k) {
            //TODO 检索
            return null;
        }

        final void removeTreeNode(QHashMap<K, V> map, Node<K, V>[] tab) {
            //TODO
        }

        final void split(QHashMap<K, V> map, Node<K, V>[] tab, int index, int bit) {
            //TODO
        }

    }

}
