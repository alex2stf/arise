package com.arise.cargo.management;

import com.arise.core.models.Handler;
import com.arise.core.tools.Assert;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Map;

import static com.arise.cargo.management.DependencyManager.importDependencyRules;
import static com.arise.cargo.management.DependencyManager.withJar;
import static com.arise.core.tools.Assert.assertEquals;
import static com.arise.core.tools.Assert.assertFailed;
import static com.arise.core.tools.Assert.assertNotNull;


public class DependencyManagerTest {


    public static void test1() throws IOException {

        Map<String, Object>  res = DependencyManager.solve("JAVAZOOM_JLAYER_101");
        Assert.assertTrue((res.get("jar-location") + "").endsWith("jlayer-1.0.1.jar"));
        withJar("JAVAZOOM_JLAYER_101", new Handler<URLClassLoader>() {
            @Override
            public void handle(URLClassLoader classLoader) {
                try {
                    Class c = Class.forName("javazoom.jl.player.Player", true, classLoader);
                    assertNotNull(c);
                } catch (ClassNotFoundException e) {
                    assertFailed(e);
                }
            }
        });


        DependencyManager.withBinary("MEDIA_INFO_CLI_WIN32", new Handler<File>() {
            @Override
            public void handle(File file) {
                Assert.assertEquals("MediaInfo.exe", file.getName());
            }
        });



    }

    public static void main(String[] args) throws IOException {
//

        importDependencyRules("_cargo_/dependencies.json");
        DependencyManagerTest.test1();
       Map<String, Object> res = DependencyManager.solve("MEDIA_INFO_CLI_WIN32");


        System.out.println(res);
    }

}