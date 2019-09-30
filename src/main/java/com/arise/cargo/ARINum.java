package com.arise.cargo;

public class ARINum extends ARIType {


    private final String[] variants;

    public ARINum(String name, String[] namespaces, String[] variants) {
        super(name, namespaces, false);
        this.variants = variants;
    }

    public String[] getVariants() {
        return variants;
    }
}
