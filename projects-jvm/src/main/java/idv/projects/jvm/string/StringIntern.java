package idv.projects.jvm.string;


/**
 * @author shaoq 2021/2/28 13:52
 */
public class StringIntern {

    public static void main(String[] args) {

        String s1 = new String("1");
        String s2 = s1.intern();
        String s3 = "1";
        System.out.println(s1 == s3); // false
        System.out.println(s2 == s3); // true


        /*
            对象1：new StringBuilder()
            对象2：new String("a")
            对象3：常量池a
            对象4：new String("b")
            对象5：常量池b
            对象6：new String("ab")
         */
        String s4 = new String("a") + new String("b");
        String s5 = s4.intern();
        String s6 = "ab";
        System.out.println(s4 == s5); // true
        System.out.println(s4 == s6); // true
    }

}
