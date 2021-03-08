package idv.projects.spring.bean;

import lombok.Data;

/**
 * @author shaoq 2021/3/5 13:41
 */
@Data
public class B {


    private A a;

    public B(A a) {
        this.a = a;
    }


}
