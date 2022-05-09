package com.arise.canter;

import com.arise.core.exceptions.LogicalException;
import com.arise.core.tools.StringUtil;

import java.util.Map;

import static com.arise.canter.Arguments.shouldParse;
import static com.arise.core.tools.CollectionUtil.isEmpty;

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
        Task task = registry.getCommand(commandId);

        String[] localArgs = new String[]{};
        if (!isEmpty(arguments)){
            localArgs = new String[arguments.length];
            for (int i = 0; i < arguments.length; i++){
                String currentArg = arguments[i];
                if (shouldParse(arguments[i])){
                    Map<String, Object> mapArgs = args.getMapArgs();
                    localArgs[i] = StringUtil.map(arguments[i], mapArgs, registry.getFieldCriteria(), registry.getMethodCriteria());
                }
                else if (currentArg.startsWith("$")){
                    int argBegin = currentArg.indexOf("(");
                    int argEnd = currentArg.indexOf(")");
                    if (argBegin < 0 || argEnd < 0){
                        throw new LogicalException("any nested method should be called with ()");
                    }
                    String nsargs[] = currentArg.substring(argBegin + 1, argEnd).split(",");
                    String commandId = currentArg.substring(1, argBegin);
                    localArgs[i] = registry.execute(commandId, nsargs)  + "";
                }
                else {
                    localArgs[i] = arguments[i];
                }
            }
        }

        Object response = null;
        Throwable err = null;
        try {
            response = task.execute(localArgs);
        } catch (Throwable t){
            t.printStackTrace();
            err = t;
        }
        if (err != null &&  onError != null){
            registry.dispatch(onError, task, err);
        } else if (onSuccess != null){
            registry.dispatch(onSuccess, task, response);
        }

        System.out.println("execute task [" + task.getId() + ": "
                + StringUtil.join(localArgs, " ")
                + "] on thread (" + Thread.currentThread().getName() + ") into <" + returnName + ">"
                + " value = " + response);

        return response;
    }

    public boolean isAsync() {
        return async;
    }





}
