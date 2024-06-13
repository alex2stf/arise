package com.arise.canter;

public class CommandEvent {
    private final String name;
    private final String[] visibility;

    public CommandEvent(String name, String[] visibility) {
        this.name = name;
        this.visibility = visibility;
    }

    public String getName() {
        return name;
    }

    public String[] getVisibility() {
        return visibility;
    }
}
