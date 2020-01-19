package com.arise.canter;

public class Event {
    private final String name;
    private final String[] visibility;

    public Event(String name, String[] visibility) {
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
