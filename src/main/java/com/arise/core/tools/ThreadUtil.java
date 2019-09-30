package com.arise.core.tools;

public class ThreadUtil {
    public static Thread startThread(Runnable action, String name){
        Thread t = new Thread(action);
        if (name != null){
            t.setName(name);
        }
        t.start();
        return t;
    }

    public static Thread startThread(Runnable action){
        return startThread(action, null);
    }



}
