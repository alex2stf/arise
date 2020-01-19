package com.arise.canter;

public class EventHandler extends Task {
    private final String id;
    private final Event event;

    public EventHandler(String id, Event event) {
        this.event = event;
        this.id = id;
    }

    @Override
    public final Object execute(String... args) {
        Arguments arguments = new Arguments();
        batchExecute(executions, arguments, getRegistry().newAsyncExecutor());
        return arguments;
    }

    @Override
    public final String getId() {
        return id;
    }

    public final Event getEvent() {
        return event;
    }


    public Object process(Object[] args) {
        String[] x = new String[args.length];
        for (int i = 0; i < args.length; i++){
            x[i] = String.valueOf(args[i]);
        }
        return execute(x);
    }
}
