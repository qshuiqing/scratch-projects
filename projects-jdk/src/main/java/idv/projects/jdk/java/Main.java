package idv.projects.jdk.java;

import org.openjdk.jol.info.ClassLayout;

public class Main {


    public static void main(String[] args) {

        Object o = new Object();

        System.out.println(ClassLayout.parseInstance(o).toPrintable());

    }

}
