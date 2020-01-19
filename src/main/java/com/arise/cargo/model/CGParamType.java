package com.arise.cargo.model;

import java.util.Date;

public class CGParamType {
    String[] typedParameters;

    public String[] getTypedParameters() {
        return typedParameters;
    }

    public String getTimestamp(){
        return new Date().toString();
    }

    public boolean hasTypedParameters(){
        return typedParameters != null && typedParameters.length > 0;
    }
}
