package idv.projects.jvm.subsystem.classloader;

import java.util.List;

/**
 * @author qshuiqing 2021/2/23 9:52 上午
 */
public abstract class Loading implements List {

    private int m;

    public int inc() {
        Object n = new Object();
        return m + 1;
    }


}
