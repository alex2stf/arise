package com.arise.canter;

import static com.arise.core.tools.CollectionUtil.isEmpty;


@Deprecated
public class Execution {
    final Registry registry;
    final String returnName;
    final String commandId;
    final boolean async;
    protected final String[] arguments;
    final Event[] onSuccess;
    final Event[] onError;

    Execution(Registry registry, String returnName, String commandId, boolean async, String[] arguments,
              Event[] onSuccess, Event[] onError) {
        this.registry = registry;
        this.returnName = returnName;
        this.commandId = commandId;
        this.async = async;
        this.arguments = arguments;
        this.onSuccess = onSuccess;
        this.onError = onError;
    }




    public Object execute(Arguments args){
        Command command = registry.getCommand(commandId);

        String[] localArgs = new String[]{};
        if (!isEmpty(arguments)){
            localArgs = new String[arguments.length];
            for (int i = 0; i < arguments.length; i++){
                String currentArg = arguments[i];
                if (currentArg.startsWith("$")){
                    localArgs[i] = registry.executeCmdLine(currentArg) + "";
                }
                else {
                    localArgs[i] = arguments[i];
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
            registry.dispatch(onError, command, err);
        } else if (onSuccess != null){
            registry.dispatch(onSuccess, command, response);
        }

//        Mole.getInstance(Execution.class).log("execute task [" + task.getId() + ": "
//                + StringUtil.join(localArgs, " ")
//                + "] on thread (" + Thread.currentThread().getName() + ") into <" + returnName + ">"
//                + " value = " + response);

        return response;
    }

    public boolean isAsync() {
        return async;
    }





}
