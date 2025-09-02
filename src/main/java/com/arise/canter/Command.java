package com.arise.canter;

import com.arise.core.tools.GenericTypeWorker;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StringUtil;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;


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
                        .objectEnd().build();
    }


    public void dispatchError(Throwable e){
//        e.printStackTrace();
        Mole.getInstance(Command.class).e("Supressed error " + e.getMessage());
    }





    public T execute(String ... args){
        return execute(asList(args));
    }

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




    public static List<String> parseArgs(List<String> in, List<String> arguments){
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
