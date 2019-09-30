package com.arise.data;

public class SimpleDTO {
    private int intNr;
    private double doubleNr;
    private boolean bool;
    private String str;

    public SimpleDTO(){

    }

    public int getIntNr() {
        return intNr;
    }

    public void setIntNr(int intNr) {
        this.intNr = intNr;
    }

    public double getDoubleNr() {
        return doubleNr;
    }

    public void setDoubleNr(double doubleNr) {
        this.doubleNr = doubleNr;
    }

    public boolean isBool() {
        return bool;
    }

    public void setBool(boolean bool) {
        this.bool = bool;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }
}
