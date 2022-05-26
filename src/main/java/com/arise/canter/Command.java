package com.arise.canter;

import com.arise.core.tools.GenericTypeWorker;
import com.arise.core.tools.MapUtil;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.ThreadUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.arise.core.tools.ThreadUtil.fireAndForget;


public abstract class Command<T> extends GenericTypeWorker {

    protected final String id;
    protected String usage = "-";

    public String getUsage() {
        return usage;
    }

    public String getDescription() {
        return description;
    }

    protected String description = "-";
    private CommandRegistry commandRegistry;



    public Command(String id){
        this.id = id;
    }


    @Override
    public String toString() {
        return StringUtil.jsonBuilder().objectStart()
                        .add("id", id)
//                        .add("arg-names", argumentsNames, QUOTE_ITERATOR)
//                        .add("properties", properties, QUOTE_ITERATOR)
                        .objectEnd().build();
    }





    public T execute(String ... args){
        return execute(Arrays.asList(args));
    };

    public abstract T execute(List<String> arguments);


    public String getId() {
        return id;
    }

    Command setRegistry(CommandRegistry commandRegistry) {
        this.commandRegistry = commandRegistry;
        return this;
    }

    public CommandRegistry getRegistry() {
        return commandRegistry;
    }

    public Object getProperty(String x) {
        return null;
    }


    static class JsonCommand extends Command<String> {

        List<Map> cmds;
        String returnValue;
        public JsonCommand(String id) {
            super(id);
        }

        private void storeResultIfNecessary(Object o, Map c){
            String storeKey = MapUtil.getString(c, "store-key");
            if (StringUtil.hasText(storeKey)){
                getRegistry().store(storeKey, o);
            }
        }

        @Override
        public String execute(final List<String> arguments) {
           final Object res[] = new Object[]{null};
           for (final Map c: cmds){
               final String commandId = MapUtil.getString(c, "id");
               final List<String> args = MapUtil.getList(c, "args");
               String asyncMode = MapUtil.getString(c, "async");

               if ("daemon".equalsIgnoreCase(asyncMode)){
                   fireAndForget(new Runnable() {
                       @Override
                       public void run() {
                           res[0] = getRegistry().execute(commandId, Command.parseArgs(args, arguments));
                           storeResultIfNecessary(res[0], c);
                       }
                   }, ThreadUtil.threadId("async-cmd-" + commandId), true);
               } else {
                   res[0] = getRegistry().execute(commandId, Command.parseArgs(args, arguments));
                   storeResultIfNecessary(res[0], c);
               }



           }
           if(StringUtil.hasText(returnValue)) {
               return String.valueOf(getRegistry().executeCmdLine(returnValue));
           }
           return String.valueOf(res[0]);
        }
    }




    private static List<String> parseArgs(List<String> in, List<String> arguments){
        List<String> cp = new ArrayList<>();
        for (String s: in){
            Integer index = argIndex(s);
            if (index != null && arguments.size() - 1 >= index){
                cp.add(arguments.get(index));
            } else {
                cp.add(s);
            }
        }
        return cp;
    }

    private static Integer argIndex(String s) {

        if (s.startsWith("{") && s.endsWith("}")){
            String in = s.substring(1, s.length() - 1);
            try {
                Integer x = Integer.valueOf(in);
                return x;
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }


}
