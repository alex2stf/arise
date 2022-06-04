package com.arise.cargo;

import com.arise.cargo.management.DependencyManager;
import com.arise.core.models.Handler;

import java.net.URLClassLoader;

public class CargoMain {



    public static void main(String[] args) throws Exception {
//        String mode = args[0];
        DependencyManager.importDependencyRules("_cargo_/dependencies.json");

        DependencyManager.withJar("JAVAZOOM_JLAYER_101", new Handler<URLClassLoader>() {
            @Override
            public void handle(URLClassLoader urlClassLoader) {
                Class classToLoad = null;
                try {
                    classToLoad = Class.forName("javazoom.jl.player.Player", true, urlClassLoader);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                System.out.println(classToLoad);
            }
        });
    }
}
