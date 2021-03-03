package idv.projects.jvm.memory;

import java.util.ArrayList;
import java.util.List;

/**
 * VM Args: -Xms20m -Xmx20m -XX:+HeapDumpOnOutOfMemoryError
 *
 * @author shaoq 2021/3/2 23:37
 */
public class HeapOOM {

    static class OOMObject {

    }

    public static void main(String[] args) throws InterruptedException {

        Thread thread = new Thread(() -> {
            List<Object> list = new ArrayList<>();
            while (true) {
                list.add(new OOMObject());
            }
        });

        thread.start();


        Thread.sleep(10000);
    }

}
