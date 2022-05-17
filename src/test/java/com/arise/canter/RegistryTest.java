package com.arise.canter;


import com.arise.core.tools.Assert;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.SYSUtils;

import java.io.File;
import java.util.UUID;

public class RegistryTest {

    public static void test_1(){
        Registry registry = new Registry();
        registry.addCommand(Defaults.PROCESS_EXEC);
        registry.addCommand(Defaults.PROCESS_EXEC);

        String test;

        if (SYSUtils.isWindows()) {
            test = (String) registry.execute("process-exec", new String[]{"cmd.exe", "/c", "echo", "hi"});
            Assert.assertEquals("hi", test);

            test = (String) registry.execute("process-exec", new String[]{"cmd.exe", "/c", "echo", "$sum(1,2)"});
            Assert.assertEquals("3", test);


            String id = UUID.randomUUID() + "-test-file.test-file";
            File f = new File(id);
            f.deleteOnExit();
            FileUtil.writeStringToFile(f, id);
            test = (String) registry.execute("process-exec", new String[]{"cmd.exe", "/c", "echo", "$valid-file("+id+",other.sh)"});
            Assert.assertEquals(id, new File(test).getName());
            f.delete();
        }

        test = (String) registry.execute("get-date", new String[]{});
        System.out.println("get-date cmd default" + test);
        Assert.assertNotNull(test);

        test = (String) registry.execute("get-date", new String[]{"yyyy-MM-dd"});
        System.out.println("get-date cmd custom" + test);
        Assert.assertNotNull(test);
    }

    public static void main(String[] args) {

        RegistryTest.test_1();
    }
}