package idv.projects.jvm.subsystem.classloader;

/**
 * @author shaoq 2021/2/23 22:19
 */
public class StaticClass {

    public static void Method() {
        String a = new String("abccccc").intern();
        int b = 2 * (2 + 6);
    }

    public static void main(String[] args) {
        StaticClass.Method();
    }

}
