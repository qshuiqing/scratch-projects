package idv.projects.spring;

import idv.projects.spring.bean.A;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author shaoq 2021/3/5 13:41
 */
public class Main {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("circular-dependencies-beans.xml");
        A a = context.getBean(A.class);
        a.getMsg();
    }

}
