package com.arise.canter;


import com.arise.core.tools.Assert;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.SYSUtils;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class CommandRegistryTest {

    public static void test_1(){
        CommandRegistry commandRegistry = CommandRegistry.INSTANCE;
        commandRegistry.addCommand(DefaultCommands.PROCESS_EXEC);
        commandRegistry.addCommand(DefaultCommands.PROCESS_EXEC);

        Object result;

        if (SYSUtils.isWindows()) {
            result = commandRegistry.execute("process-exec", new String[]{"cmd.exe", "/c", "echo", "hi"});
            Assert.assertEquals("hi", result);

            result =  commandRegistry.execute("process-exec", new String[]{"cmd.exe", "/c", "echo", "$sum(1,2)"});
            Assert.assertEquals("3", result);


            String id = UUID.randomUUID() + "-test-file.test-file";
            File f = new File(id);
            f.deleteOnExit();
            FileUtil.writeStringToFile(f, id);
            result = (String) commandRegistry.execute("process-exec", new String[]{"cmd.exe", "/c", "echo", "$valid-file("+id+",other.sh)"});
            Assert.assertEquals(id, new File(result + "").getName());
            f.delete();
        }

        result = commandRegistry.execute("get-date", new String[]{});
        System.out.println("get-date cmd default" + result);
        Assert.assertNotNull(result);

        result = commandRegistry.execute("get-date", new String[]{"yyyy-MM-dd"});
        System.out.println("get-date cmd custom" + result);
        Assert.assertNotNull(result);
    }

    public static void test_2(){
        CommandRegistry commandRegistry = CommandRegistry.getInstance();

        commandRegistry.addCommand(new Command<String>("test1") {
            @Override
            public String execute(List<String> arguments) {
                return "test1" + arguments.get(0);
            }
        });

        commandRegistry.addCommand(new Command<String>("test2") {
            @Override
            public String execute(List<String> arguments) {
                return "test2" + arguments.get(0);
            }
        });

        String res;

        res = String.valueOf(commandRegistry.executeCommandLine("$test1($test2(x))"));
        Assert.assertEquals("test1test2x", res);

        res = String.valueOf(commandRegistry.executeCommandLine("$test1(x)"));
        Assert.assertEquals("test1x", res);


    }

    public static void main(String[] args) {

        CommandRegistryTest.test_2();
        CommandRegistryTest.test_1();
    }
}