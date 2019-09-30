package com.arise.cargo;

public class ARIGeneric extends ARIType {

    private final int index;

    public ARIGeneric(String name, int index) {
        super(name, null, false, null);
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
