package com.arise.core.tools;

public class LineBuilder {

    public String getTab() {
        return tab;
    }

    protected final String tab;
    protected final StringBuilder builder;
    private boolean started = false;

    public LineBuilder(String tab){
        this.tab = tab;
        this.builder = new StringBuilder();
    }

    public LineBuilder writeLine(String ... args){
        builder.append(tab);
        for (String s: args){
            builder.append(s);
        }
        builder.append("\n");
        started = true;
        return this;
    }

    @Override
    public String toString() {
        return builder.toString();
    }

    public boolean isStarted() {
        return started;
    }

    public LineBuilder endl() {
        builder.append("\n");
        return this;
    }
}
