package com.arise.canter;


import com.arise.canter.Command.JsonCommand;
import com.arise.core.exceptions.LogicalException;
import com.arise.core.models.AsyncExecutor;
import com.arise.core.models.ThreadBatch;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.GenericTypeWorker;
import com.arise.core.tools.MapUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.arise.canter.Defaults.CMD_FIND_FILE_STREAM;
import static com.arise.canter.Defaults.CMD_GET_DATE;
import static com.arise.canter.Defaults.CMD_PRINT;
import static com.arise.canter.Defaults.CMD_SUM;
import static com.arise.canter.Defaults.CMD_VALID_FILE;
import static com.arise.canter.Defaults.PROCESS_EXEC_PRINT;

public final class Registry extends GenericTypeWorker {

    private Map<String, Command> commands = new HashMap<>();
    private Map<String, EventHandler> eventHandlers = new HashMap<>();
    private static final Mole log = Mole.getInstance(Registry.class);



    @Override
    public String toString() {

        return StringUtil.jsonBuilder()
                .objectStart()
                .add("commands", commands.values())
                .objectEnd()
                .build();
    }

    public Registry(){
       registerDefaultCommands();
    }



    public void registerDefaultCommands(){
        addCommand(CMD_PRINT);
        addCommand(CMD_GET_DATE);
        addCommand(CMD_SUM);
        addCommand(CMD_VALID_FILE);
        addCommand(CMD_FIND_FILE_STREAM);
        addCommand(PROCESS_EXEC_PRINT);
        addCommand(new Command<String>("read-storage") {
            @Override
            public String execute(Arguments arguments) {
                String key = arguments.get(0);
                return storage.get(key) + "";
            }
        });
    }






    public Command getCommand(String name) {
        return commands.get(name);
    }

    public <T> Registry addCommand(Command<T> command) {
        if (!commands.containsKey(command.getId())){
            commands.put(command.getId(), command);
        }
        command.setRegistry(this);
        return this;
    }




    public Object execute(String cmdId, String[] args){
        return execute(cmdId, args, null, null);
    }

    public Object execute(String cmdId, Arguments args){
        return execute(cmdId, args, null, null);
    }

    public Object execute(String commandId, Arguments arguments, Event[] onSuccess, Event[] onError) {
        return newExecution(commandId, arguments, onSuccess, onError).execute(arguments);
    }

    public Execution newExecution(String commandId, Arguments arguments, Event[] onSuccess, Event[] onError){
        return new Execution(this, UUID.randomUUID().toString(), commandId,
                false, arguments.array(), onSuccess, onError);
    }

    public Object executeCmdLine(String line){
        if (line.startsWith("$")){
            int argBegin = line.indexOf("(");
            int argEnd = line.indexOf(")");
            if (argBegin < 0 || argEnd < 0){
                throw new LogicalException("any nested method should be called with ()");
            }
            String nsargs[] = line.substring(argBegin + 1, argEnd).split(",");
            String commandId = line.substring(1, argBegin);
            return execute(commandId, nsargs);
        }
        return line;
    }

    public Object execute(String commandId, String[] args, Event[] onSuccess, Event[] onError) {
        return new Execution(this, UUID.randomUUID().toString(), commandId,
                false, args, onSuccess, onError).execute(Arguments.fromList(args));
    }

    public void dispatch(Event [] events, Command parentCommand, Object ... args) {
        for (Event event: events){
            for (EventHandler handler: eventHandlers.values()){
                if (handler.getEvent().getName().equals(event.getName())){
                    handler.process(args);
                }
            }
        }
    }

    public Registry loadJsonResource(String s) {
        InputStream inputStream = FileUtil.findStream(s);
        String content = StreamUtil.toString(inputStream).replaceAll("\r\n", " ");
        return loadJsonString(content);
    }

    public Registry loadJsonString(String content) {
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
        jsonCommand.returnValue = MapUtil.getString(m, "return-value");
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
