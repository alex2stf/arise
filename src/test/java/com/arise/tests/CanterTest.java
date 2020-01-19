package com.arise.tests;

import com.arise.canter.Arguments;
import com.arise.canter.Command;
import com.arise.canter.Event;
import com.arise.canter.EventHandler;
import com.arise.canter.Registry;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.StreamUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.arise.canter.Defaults.CMD_PRINT;
import static com.arise.canter.Defaults.EVT_FAIL;
import static com.arise.canter.Defaults.EVT_SUCCESS;
import static com.arise.canter.Defaults.PROCESS_EXEC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class CanterTest {

    @Test
    public void test1(){
        Command command = CMD_PRINT;
        assertEquals("test 1", command.execute("test", "1"));
    }

    @Test
    public void test2() throws InterruptedException {

        Registry registry = new Registry();


        Map<String, Object> properties = new HashMap<>();
        properties.put("prop1", "{arg1}");
        properties.put("prop1", "{arg2}");
        properties.put("prop3", "{print_result}");

        Command deviceStatTask = new Command<CustomObject>("deviceStat") {
            @Override
            public CustomObject execute(Arguments arguments) {
                return new CustomObject();
            }
        };

        registry.addCommand(deviceStatTask);

        Command dummyTask = new Command("dummy") {
            @Override
            public Object execute(Arguments arguments) {
                return "DUMMY TASK CALLED " + arguments.get(0);
            }
        };

        registry.addCommand(dummyTask);

        dummyTask
                .setArgumentNames("arg1", "arg2")
                .addExecution(true, "print_result", "print", null, null,"{arg1}", "{arg2}")
                .addExecution(true, "print_result2", "print", null, null, "hello", "world")
                .addExecution(true, "deviceStat", "deviceStat")
        ;



        Map<String, Object> innerProp = new HashMap<>();
        innerProp.put("os_version", "{deviceStat.os.version}");
        innerProp.put("second", new CustomObject());


        dummyTask.setProperties(properties)
                .addProperty("key", "value")
                .addProperty("os_name", "{deviceStat.os.name}")
                .addProperty("inner", innerProp)
        ;


        Arguments arguments = dummyTask.buildArguments("unu", "doi", "trei");
        assertEquals(3, arguments.list().size());
        assertEquals("unu", arguments.get(0));
        assertEquals("doi", arguments.get(1));
        assertEquals("trei", arguments.get(2));
        assertEquals("unu", arguments.get("arg1"));
        assertEquals("doi", arguments.get("arg2"));
        assertEquals("TESTOs", arguments.get("os_name"));


        Map innerAfter = (Map)arguments.get("inner");
        assertEquals("45", innerAfter.get("os_version"));
        Map second = (Map) innerAfter.get("second");
        Map os = (Map) second.get("os");
        assertEquals(45, os.get("version"));
        assertEquals("TESTOs", os.get("name"));
        assertEquals("TESTOs", arguments.find("inner", "second", "os", "name"));


        registry.addEvent(EVT_SUCCESS);
        assertEquals("DUMMY TASK CALLED test", dummyTask.execute("test"));

    }

    @Test
    public void testEvents(){
        Registry registry = new Registry();
        registry.addCommand(CMD_PRINT);

        boolean handlerExecuted[] = {false};
        registry.addCommand(new Command<Object>("handler-test-execution") {
            @Override
            public Object execute(Arguments arguments) {
                Assert.assertEquals("handler called", arguments.get(0));
                handlerExecuted[0] = true;
                return null;
            }
        });

        EventHandler eventHandler = new EventHandler("my-handler", EVT_SUCCESS);
        registry.addEventHandler(eventHandler);

        eventHandler.addExecution(false, "xxxx", "handler-test-execution",
                null, null, "handler called");

        registry.execute("print", new String[]{"arg"}, new Event[]{EVT_SUCCESS}, new Event[]{EVT_FAIL});

        assertTrue(handlerExecuted[0]);
    }

    @Test
    public void testCoronaUtils(){
        Registry registry = new Registry();
        registry.addCommand(PROCESS_EXEC);

        registry.loadJsonResource("src/main/resources#/corona/config/win/commands.json");

        registry.execute("closeProcessByName", new String[]{"firefox"}, null, null);




    }






    public class CustomHandler extends EventHandler {
        public CustomHandler(Event event) {
            super("my-custom-handler", event);
        }
    }



    public class CustomObject  {
        InnerOs os = new InnerOs();
    }

    private class InnerOs {
        String name = "TESTOs";
        int version = 45;
    }
}
