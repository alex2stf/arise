package com.arise.core.tools;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.arise.core.tools.CollectionUtil.pickOne;
import static com.arise.core.tools.Mole.logInfo;
import static java.util.Arrays.asList;

public class CollectionUtilTest {


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

    }

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

    public static void main(String[] args) {
        Set<String> s = new HashSet<>();
        for (int i = 0; i < 100; i++){
            String x = pickOne(asList("1", "2","4", "3"));
            s.add(x);
            logInfo("pickOne(1, 2, 3, 4) = " + x );
        }
        Assert.assertTrue(s.contains("1"));
        Assert.assertTrue(s.contains("2"));
        Assert.assertTrue(s.contains("3"));
        Assert.assertTrue(s.contains("4"));

    }
}
