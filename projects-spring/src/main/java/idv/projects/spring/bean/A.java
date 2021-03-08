package idv.projects.spring.bean;

import lombok.Data;

/**
 * @author shaoq 2021/3/5 13:41
 */
@Data
public class A {

    private B b;

    public A() {

    }

    public A(B b) {
        this.b = b;
    }

    public void getMsg() {
        System.out.println("This is 'A' class!");
    }

}
