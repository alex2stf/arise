package com.arise.canter;

import com.arise.core.tools.GenericTypeWorker;
import com.arise.core.tools.MapUtil;
import com.arise.core.tools.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.arise.core.tools.CollectionUtil.isEmpty;


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
    private Registry registry;



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





    public final T execute(String ... args){
        Arguments arguments = Arguments.fromList(args);
        return execute(arguments);
    };

    public abstract T execute(Arguments arguments);


    public String getId() {
        return id;
    }

    Command setRegistry(Registry registry) {
        this.registry = registry;
        return this;
    }

    public Registry getRegistry() {
        return registry;
    }



    static class JsonCommand extends Command<String> {

        List<Map> cmds;
        String returnValue;
        public JsonCommand(String id) {
            super(id);
        }

        @Override
        public String execute(Arguments arguments) {
           Object o = null;
           for (Map c: cmds){
               String commandId = MapUtil.getString(c, "id");
               List<String> args = MapUtil.getList(c, "args");
               o = getRegistry().execute(commandId, Arguments.fromCollection(
                       Command.parseArgs(args, arguments)
               ));
               String storeKey = MapUtil.getString(c, "store-key");
               if (StringUtil.hasText(storeKey)){
                   getRegistry().store(storeKey, o);
               }
           }
           if(StringUtil.hasText(returnValue)) {
               return String.valueOf(getRegistry().executeCmdLine(returnValue));
           }
           return String.valueOf(o);
        }
    }

    private static List<String> parseArgs(List<String> in, Arguments arguments){
        List<String> cp = new ArrayList<>();
        for (String s: in){
            Integer index = argIndex(s);
            if (index != null && arguments.hasIndex(index)){
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
