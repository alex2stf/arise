package com.arise.core.tools;

import net.bytebuddy.build.ToStringPlugin;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class CollectionUtilTest {
    @Test
    public void testDiff() {
        List<String> old = new ArrayList<>();
        old.add("a");
        old.add("b");
        old.add("c");

        List<String> nVal = new ArrayList<>();
        nVal.add("a");
        nVal.add("b");
        nVal.add("e");

        CollectionUtil.Handler<String> handler = new CollectionUtil.Handler<String>() {
            @Override
            public void added(String s) {
                System.out.println("ADDED " + s);
            }

            @Override
            public void removed(String s) {
                System.out.println("REMOVED " + s);
            }

            @Override
            public void same(String s) {
                System.out.println("SAME " + s);
            }
        };
        CollectionUtil.scan(nVal, old, handler);

//        CollectionUtil.scan(null, old, handler);
//        CollectionUtil.scan(nVal, null, handler);
    }

    @Test
    public void testIterate(){
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        CollectionUtil.smartIterate(list, new CollectionUtil.SmartHandler<Integer>() {
            @Override
            public void handle(Integer t1, Integer t2) {
                System.out.println(t1 + " " + t2);
            }
        });
    }
}
