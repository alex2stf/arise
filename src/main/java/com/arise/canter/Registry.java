package com.arise.canter;


import com.arise.core.exceptions.LogicalException;
import com.arise.core.models.AsyncExecutor;
import com.arise.core.models.Handler;
import com.arise.core.models.ThreadBatch;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.GenericTypeWorker;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.arise.canter.Defaults.CMD_GET_DATE;
import static com.arise.canter.Defaults.CMD_PRINT;
import static com.arise.canter.Defaults.CMD_SUM;

public final class Registry extends GenericTypeWorker {

    private Map<String, Command> commands = new HashMap<>();
    private Map<String, EventHandler> eventHandlers = new HashMap<>();
    private List<Event> events = new ArrayList<>();
    private Class<? extends AsyncExecutor> asyncExecutorClass = ThreadBatch.class;
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





    public Registry addEvent(Event e) {
        events.add(e);
        return this;
    }

    public Registry setAsyncExecutor(Class<? extends AsyncExecutor> tClass){
        asyncExecutorClass = tClass;
        return this;
    }

    public Object execute(String cmdId, String[] args){
        return execute(cmdId, args, null, null);
    }

    public Object execute(String cmdId, Arguments args){
        return execute(cmdId, args, null, null);
    }

    public Object execute(String commandId, Arguments arguments, Event[] onSuccess, Event[] onError) {
        return new Execution(this, UUID.randomUUID().toString(), commandId,
                false, arguments.array(), onSuccess, onError).execute(arguments);
    }

    public Object execute(String commandId, String[] args, Event[] onSuccess, Event[] onError) {
        return new Execution(this, UUID.randomUUID().toString(), commandId,
                false, args, onSuccess, onError).execute(Arguments.fromList(args));
    }

    public void dispatch(Event [] events, Task parentTask, Object ... args) {
        for (Event event: events){
            for (EventHandler handler: eventHandlers.values()){
                if (handler.getEvent().getName().equals(event.getName())){
                    handler.process(args);
                }
            }
        }
    }

    public Registry addEventHandler(EventHandler eventHandler) {
        eventHandlers.put(eventHandler.getId(), eventHandler);
        eventHandler.setRegistry(this);
        return this;
    }

    public List<Event> getEvents(){
        return events;
    }


    AsyncExecutor newAsyncExecutor() {
        try {
            return asyncExecutorClass.newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    public Registry loadJsonResource(String s) {
        InputStream inputStream = FileUtil.findStream(s);
        String content = StreamUtil.toString(inputStream).replaceAll("\r\n", " ");
        List arr = (List) Groot.decodeBytes(content);
        for (Object obj: arr){
            importJsonObject((Map) obj);
        }
        return this;
    }

    private void importJsonObject(Map obj) {
        String parentType = (String) obj.get("parent");

        final Command parentCmd = getCommand(parentType);
        if (parentCmd == null){
            throw  new LogicalException("Missing schema " + parentType);
        }

        String id = (String) obj.get("id");

        Command nextCmd = new Command(id) {

            final Command parent = parentCmd;
            @Override
            public Object execute(Arguments arguments) {
                return parent.setArgumentNames(this.argumentsNames)
                        .setProperties(this.properties).execute(arguments);
            }
        };

        List arr = (List) obj.get("arguments");
        String args[] = new String[arr.size()];
        for (int i = 0; i < arr.size(); i++){
            args[i] = String.valueOf(arr.get(i));
        }
        nextCmd.setArgumentNames(args);
        Map<String, Object> props = (Map<String, Object>) obj.get("properties");


        Map<String, Object> properties = new HashMap<>();
        for (Map.Entry<String, Object> entry: props.entrySet()){
            properties.put(entry.getKey(), entry.getValue());
        }
        nextCmd.setProperties(properties);

        log.info("import cmd " + id + " using schema " + parentCmd);
        addCommand(nextCmd);

    }


}
