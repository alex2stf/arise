package com.arise.cargo.management;

import java.io.File;


@Deprecated
public abstract class Rule  {
    private File zipped;

    public  abstract boolean acceptConditions();
    public  abstract String getPath();

    String getSourceName(){
        String parts[] = getPath().split("/");
        String name = parts[parts.length -1];
        if (name.indexOf("?") > -1){
            return name.split("\\?")[0];
        }
        return name;
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
