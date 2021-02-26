package idv.projects.jdk.java;

import org.openjdk.jol.info.ClassLayout;

public class Main {


    public static void main(String[] args) {

//        DO[] o = {new DO(), new DO()};

        int[] i = {1,2,3};

//        System.out.println(ClassLayout.parseInstance(o).toPrintable());
        System.out.println(ClassLayout.parseInstance(i).toPrintable());
    }

}
