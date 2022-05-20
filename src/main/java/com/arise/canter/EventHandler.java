package com.arise.canter;

public class EventHandler {
    private final String id;
    private final Event event;
    private Registry registry;

    public EventHandler(String id, Event event) {
        this.event = event;
        this.id = id;
    }

    public final Object execute(String... args) {
        Arguments arguments = new Arguments();
        return arguments;
    }

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

    EventHandler setRegistry(Registry registry) {
        this.registry = registry;
        return this;
    }
}
