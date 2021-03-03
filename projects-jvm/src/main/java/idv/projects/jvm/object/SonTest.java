package idv.projects.jvm.object;

/**
 * @author shaoq 2021/3/2 21:21
 */
class Father {
    /**
     * 实例化过程：
     *   - 局部变量赋值（非静态）
     *   - 代码块（非静态）
     *   - 构造函数赋值
     */

    int x = 10;

    {
        x = 101;
    }

    Father() {
        this.print();
        x = 20;
    }

    {
        x = 102;
    }

    public void print() {
        System.out.println("Father.x = " + x);
    }

}

class Son extends Father {
    int x = 30;

    public Son() {
        this.print();
        x = 40;
    }

    public void print() {
        System.out.println("Son.x = " + x);
    }

}

public class SonTest {
    public static void main(String[] args) {
//        Father father = new Son();
        Father father = new Father();
        System.out.println(father.x);
    }
}
