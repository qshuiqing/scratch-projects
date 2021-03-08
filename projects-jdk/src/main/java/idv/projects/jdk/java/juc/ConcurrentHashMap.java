package idv.projects.jdk.java.juc;

/**
 * 键值不能为 null。默认数组大小 16
 *
 * @author shaoq 2021/3/7 17:20
 */
public class ConcurrentHashMap<K, V> {

    // 数组最大容量（int 类型可表示的最大 2 的幂次方）
    public static final int MAXIMUM_CAPACITY = 1 << 30;

    // 默认数组大小
    private static final int DEFAULT_CAPACITY = 1 << 4;

    // 树化阈值
    static final int TREEIFY_THRESHOLD = 1 << 3;

    //TODO ?????
    static final int MOVED = -1;
    // 红黑树结点
    static final int TREEBIN = -2;
    // 用于 hash 取正，高位置 0
    static final int HASH_BITS = 0x7fffffff;

    static class Node<K, V> { // 普通结点
        final int hash;
        final K key;
        // 保证 val 可见性
        volatile V val;
        // 保证 next 可见性
        volatile Node<K, V> next;

        Node(int hash, K key, V val, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.val = val;
            this.next = next;
        }
    }


    // 高低16位异或，使散列分布均匀，最高位置 0（符号位）
    static final int spread(int h) {
        return (h ^ (h >>> 16)) & HASH_BITS;
    }

    // 取不小于 c 的最小 2 的幂次方的值
    // 易于取模运算
    private static final int tableSizeFor(int c) {
        int n = c - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    @SuppressWarnings("unchecked")
    static final <K, V> Node<K, V> tabAt(Node<K, V>[] tab, int i) {
        // 获取数组下标为 i 的 Node 地址
        return (Node<K, V>) U.getObjectVolatile(tab, ((long) i << ASHIFT) + ABASE);
    }

    static final <K, V> boolean casTabAt(Node<K, V>[] tab, int i, Node<K, V> c, Node<K, V> v) {
        return U.compareAndSwapObject(tab, ((long) i << ASHIFT) + ABASE, c, v);
    }

    // 数组用于存储映射的元素
    transient volatile Node<K, V>[] table;

    // rehash 阈值
    private transient volatile int sizeCtl;

    public ConcurrentHashMap(int initialCapacity) { // 初始化
        sizeCtl = tableSizeFor(initialCapacity + (initialCapacity >>> 1) + 1); // ????
    }

    public V put(K key, V value) {
        return putVal(key, value, false);
    }

    final V putVal(K key, V value, boolean onlyIfAbsent) {
        // 键值非空
        if (key == null || value == null) throw new NullPointerException();
        // 取 hash，可使散列结果更加均匀
        // 多数情况，数组长度小于 2^16，取模时只有低 16 位参与。高低异或之后取模使高位也参与运算，使最后结果更加均匀
        int hash = spread(key.hashCode());

        int binCount = 0;
        for (Node<K, V>[] tab = table; ; ) {
            Node<K, V> f;
            int n, i, fh;
            // 数组未初始化
            if (tab == null || (n = tab.length) == 0) {
                initTable();
            } else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
                // 空桶，无需加锁，基于 CAS 插入
                if (casTabAt(tab, i, null, new Node<>(hash, key, value, null))) break;
            } else if ((fh = f.hash) == MOVED) {
                //TODO helpTransfer
            } else {
                V oldVal = null;
                // 首个元素加锁，即可多线程并发操作不同的桶
                synchronized (f) {
                    // 再次确定，防止并发修改
                    if (tabAt(tab, i) == f) {
                        if (fh >= 0) {
                            // 非红黑树结点，红黑树结点为 -2
                            //TODO ?????
                            binCount = 1;
                            for (Node<K, V> e = f; ; ++binCount) {
                                K ek;
                                if (e.hash == hash && ((ek = e.key) == key || key.equals(ek))) {
                                    // 匹配到，根据标记确定是否需修改
                                    oldVal = e.val;
                                    if (!onlyIfAbsent) {
                                        // 检索不到插入，检索到更新
                                        e.val = value;
                                    }
                                    break;
                                }
                                Node<K, V> pred = e;
                                if ((e = e.next) == null) {
                                    // 未匹配到尾部插入
                                    pred.next = new Node<>(hash, key, value, null);
                                    break;
                                }
                            }
                        } else if (f instanceof TreeBin) {
                            // 红黑树结点
                            Node<K, V> p;
                            binCount = 2;
                            if ((p = ((TreeBin<K, V>) f).putTreeVal(hash, key, value)) != null) {
                                oldVal = p.val;
                                if (!onlyIfAbsent) {
                                    p.val = value;
                                }
                            }
                        }
                    }
                }
                if (binCount != 0) {
                    if (binCount >= TREEIFY_THRESHOLD) {
                        treeifyBin(tab, i);
                    }
                    if (oldVal != null) {
                        return oldVal;
                    }
                    break;
                }
            }
        }

        addCount(1L, binCount);

        return null;
    }

    public V putIfAbsent(K key, V value) {
        return putVal(key, value, true);
    }

    /* ---------------- Table Initialization and Resizing -------------- */

    private final Node<K, V>[] initTable() {
        Node<K, V>[] tab;
        int sc;
        // 数组未实例化或数组长度为 0
        while ((tab = table) == null || tab.length == 0) {
            // 未抢到锁，防止自旋，自动放弃 CPU
            if ((sc = sizeCtl) < 0) Thread.yield();
                // 基于 CAS 操作加锁
            else if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {
                try {
                    if ((tab = table) == null || tab.length == 0) {
                        int n = (sc > 0) ? sc : DEFAULT_CAPACITY;
                        @SuppressWarnings("unchecked")
                        Node<K, V>[] nt = (Node<K, V>[]) new Node<?, ?>[n];
                        table = tab = nt;
                        sc = n - (n >>> 2);//TODO 设置？？？？
                    }
                } finally {
                    sizeCtl = sc;
                }
                break;
            }
        }
        return tab;
    }

    private final void addCount(long x, int check) {
        //TODO ????
    }

    /* ---------------- Conversion from/to TreeBins -------------- */

    private final void treeifyBin(Node<K, V>[] tab, int index) {

    }


    /* ---------------- TreeNodes -------------- */

    // 红黑树结点
    static final class TreeNode<K, V> extends Node<K, V> {
        TreeNode<K, V> parent; //TODO ?????
        TreeNode<K, V> left; //TODO ?????
        TreeNode<K, V> right; //TODO ?????
        TreeNode<K, V> prev; //TODO ?????
        boolean read; //TODO ?????

        public TreeNode(int hash, K key, V val, Node<K, V> next, TreeNode<K, V> parent) {
            super(hash, key, val, next);
            this.parent = parent;
        }
    }

    static final class TreeBin<K, V> extends Node<K, V> {
        TreeNode<K, V> root; //TODO ?????
        volatile TreeNode<K, V> first; //TODO ?????
        volatile Thread waiter; //TODO ?????
        volatile int lockState; //TODO ?????

        static final int WRITER = 1; //TODO ?????
        static final int WAITER = 2; //TODO ?????
        static final int READER = 4; //TODO ?????

        TreeBin(TreeNode<K, V> b) {
            super(TREEBIN, null, null, null);
        }

        final TreeNode<K, V> putTreeVal(int h, K k, V v) {
            return null;
        }

    }

    private static final sun.misc.Unsafe U;
    private static final long SIZECTL;
    private static final long ABASE;
    private static final int ASHIFT;

    static {
        try {
            U = sun.misc.Unsafe.getUnsafe();
            Class<?> k = ConcurrentHashMap.class;
            // 获取 sizeCtl 的偏移量
            SIZECTL = U.objectFieldOffset(k.getDeclaredField("sizeCtl"));
            Class<?> ak = Node[].class;
            // 获取数组首地址
            ABASE = U.arrayBaseOffset(ak);
            int scale = U.arrayIndexScale(ak);
            // 判断 scale 是否 2 的幂次方
            if ((scale & (scale - 1)) != 0) throw new Error("data type scale not a power of two");
            ASHIFT = 31 - Integer.numberOfLeadingZeros(scale); //TODO ??????
        } catch (Exception e) {
            throw new Error(e);
        }
    }

}
