package com.arise.cargo;

import com.arise.cargo.management.DependencyManager;
import com.arise.cargo.management.Project;
import com.arise.core.models.Handler;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.ReflectUtil;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

public class CargoMain {



    public static void main(String[] args) throws Exception {
//        String mode = args[0];
        DependencyManager.importDependencyRules("_cargo_/dependencies.json");

        DependencyManager.withJarDependencyLoader("JAVAZOOM_JLAYER_101", new Handler<URLClassLoader>() {
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
//        DependencyManager.withJarDependency("JAVAZOOM_JLAYER_101", new Handler<DependencyManager.Resolution>() {
//            @Override
//            public void handle(DependencyManager.Resolution resolution) {
//                System.out.println(resolution.file());
//                File jar = resolution.file();
//
//                URLClassLoader child = null;
//                try {
//                    child = new URLClassLoader(
//                            new URL[] {jar.toURI().toURL()},
//                            this.getClass().getClassLoader()
//                    );
//                    Class classToLoad = Class.forName("javazoom.jl.player.Player", true, child);
//
//                    System.out.println(classToLoad);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }
//        });

//        System.out.println(project);
    }
}
