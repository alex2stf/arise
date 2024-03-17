package com.arise.canter;


import com.arise.canter.Command.JsonCommand;
import com.arise.core.exceptions.SyntaxException;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.arise.canter.Defaults.*;
import static com.arise.core.tools.CollectionUtil.isEmpty;
import static com.arise.core.tools.StringUtil.hasText;

public final class CommandRegistry extends GenericTypeWorker {

    private Map<String, Command> commands = new HashMap<>();
    private static final Mole log = Mole.getInstance(CommandRegistry.class);



    @Override
    public String toString() {

        return StringUtil.jsonBuilder()
                .objectStart()
                .add("commands", commands.values())
                .objectEnd()
                .build();
    }

    public CommandRegistry(){
       registerDefaultCommands();
    }



    public void registerDefaultCommands(){
        addCommand(CMD_PRINT);
        addCommand(CMD_GET_DATE);
        addCommand(CMD_SUM);
        addCommand(CMD_VALID_FILE);
        addCommand(CMD_FIND_FILE_STREAM);
        addCommand(new Command<String>("read-storage") {
            @Override
            public String execute(List<String> arguments) {
                String key = arguments.get(0);
                return storage.get(key) + "";
            }
        });

        addCommand(new Command<Process>("exec-sync") {
            @Override
            public Process execute(List<String> arguments) {
                try {
                    return Runtime.getRuntime().exec(CollectionUtil.toArray(arguments));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }






    public Command getCommand(String name) {
        return commands.get(name);
    }

    public <T> CommandRegistry addCommand(Command<T> command) {
        if (!commands.containsKey(command.getId())){
            commands.put(command.getId(), command);
        }
        command.setRegistry(this);
        return this;
    }




    public Object execute(String cmdId, String[] args){
        return execute(cmdId, args, null, null);
    }

    public Object execute(String cmdId, List<String> args){
        String[] cp = CollectionUtil.toArray(args);
        return execute(cmdId, cp, null, null);
    }


    public Object executeCmdLine(String line){
        if (line.startsWith("$")){
            int argBegin = line.indexOf("(");
            int argEnd = line.lastIndexOf(")");
            if (argBegin < 0 || argEnd < 0){
                throw new SyntaxException("Invalid sintax cmdLine [" + line + "]");
            }
            //TODO fixeaza aici splitul dupa virgula, ca nu e bine deloc!!!!
            //String nsargs[] = line.substring(argBegin + 1, argEnd).trim().split(",");
            int num = 0;
            List<String> nsargs = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            for (int i = argBegin + 1; i < argEnd; i++){
                char c = line.charAt(i);
                if (c == '('){
                    num++;
                }
                if (c == ')'){
                    num--;
                }
                if (num % 2 == 0 && c == ','){
                    nsargs.add(sb.toString().trim());
                    sb = new StringBuilder();
                } else {
                    sb.append(c);
                }
            }
            String r = sb.toString().trim();
            if (hasText(r)){
                nsargs.add(r);
            }
//            List<String> actArgs = new ArrayList<>();
//            for (String s: nsargs){
//                actArgs.add(executeCmdLine(s) + "");
//            }

            String commandId = line.substring(1, argBegin);
            return execute(commandId, nsargs);
        }
        return line;
    }

    public Object execute(String commandId, String[] args, Event[] onSuccess, Event[] onError) {

        Command command = getCommand(commandId);

        String[] localArgs = new String[]{};
        if (!isEmpty(args)){
            localArgs = new String[args.length];
            for (int i = 0; i < args.length; i++){
                String currentArg = args[i];
                if (currentArg.startsWith("$")){
                    localArgs[i] = executeCmdLine(currentArg) + "";
                }
                else {
                    localArgs[i] = args[i];
                }
            }
        }

        Object response = null;
        Throwable err = null;
        try {
            response = command.execute(localArgs);
        } catch (Throwable t){
            t.printStackTrace();
            err = t;
        }
        if (err != null &&  onError != null){
            dispatch(onError, command, err);
        } else if (onSuccess != null){
            dispatch(onSuccess, command, response);
        }
        return response;
    }

    public void dispatch(Event [] events, Command parentCommand, Object ... args) {


        System.out.println("TODO");
    }

    public CommandRegistry loadJsonResource(String s) {
        log.info("loading commands from json file " + s);
        InputStream inputStream = FileUtil.findStream(s);
        String content = StreamUtil.toString(inputStream).replaceAll("\r\n", " ");
        return loadJsonString(content);
    }

    public CommandRegistry loadJsonString(String content) {

        List arr = (List) Groot.decodeBytes(content);
        for (Object obj: arr){
            importJsonObject((Map) obj);
        }
        return this;
    }

    public void importJsonObject(Map m) {
        String id = MapUtil.getString(m, "id");
        JsonCommand jsonCommand = new JsonCommand(id);
        jsonCommand.cmds = MapUtil.getList(m, "commands");
        jsonCommand.retVal = MapUtil.getString(m, "return-value");
        addCommand(jsonCommand);
    }


    private Map<String, Object> storage = new ConcurrentHashMap<>();
    public synchronized void store(String key, Object res) {
        storage.put(key, res);
    }

    public boolean containsCommand(String key) {
        return commands.containsKey(key);
    }
}
