package com.arise.cargo.management;

import java.io.File;

public abstract class Rule {
    private File zipped;

    public  abstract boolean acceptConditions();
   public  abstract String getPath();

    String getSourceName(){
        String parts[] = getPath().split("/");
        return parts[parts.length -1];
    };

    Rule setZipped(File zipped) {
        this.zipped = zipped;
        return this;
    }

    public File getZipped() {
        return zipped;
    }

    public abstract String getName();
}
