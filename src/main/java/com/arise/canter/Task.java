package com.arise.canter;

import com.arise.core.models.AsyncExecutor;
import com.arise.core.tools.GenericTypeWorker;
import com.arise.core.tools.Provider;
import com.arise.core.tools.StringUtil;

import java.util.ArrayList;
import java.util.List;

public abstract class Task<T> extends GenericTypeWorker {
    protected List<Execution> executions = new ArrayList();


    private Registry registry;

    public static void batchExecute(List<Execution> variables, final Arguments destination, AsyncExecutor executor) {
        for (final Execution v: variables){
            if (v.isAsync()){
                executor.put(v.returnName, new Provider() {
                    @Override
                    public Object get() {
                        return v.execute(destination);
                    }
                });
            }
            else {
                destination.put(v.returnName, v.execute(destination));
            }
        }
        destination.addResults(executor.results());
    };



    /**
     * a variable supports only arguments mapping
     * @param async
     * @param resultName
     * @param taskName
     * @param onSuccess
     * @param onError
     * @param strargs
     * @return
     */
    public Task addExecution(boolean async,
                             String resultName,
                             String taskName,
                             String onSuccess,
                             String onError,
                             String ... strargs){
        if (registry == null){
            //TODO custom error
            throw new RuntimeException("cannot create variables without registry");
        }
        if (StringUtil.hasText(onSuccess)){
            System.out.println("parse TODO");
        }

        if (StringUtil.hasText(onError)){
            System.out.println("parse error TODO");
        }

        executions.add(new Execution(registry, resultName, taskName,  async,strargs, null, null));
        return this;
    }

    public void addExecution(boolean async, String resultName, String taskName) {
        addExecution(async, resultName, taskName, null, null);
    }

    public final Registry getRegistry() {
        return registry;
    }

    final Task setRegistry(Registry registry){
        this.registry = registry;
        return this;
    }

    public abstract  T execute(String ... args);

    public abstract String getId();
}
