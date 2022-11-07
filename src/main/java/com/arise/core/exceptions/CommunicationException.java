package com.arise.core.exceptions;

public class CommunicationException extends RuntimeException {


    public CommunicationException(String m, Throwable c){
        super(m, c);
    }
}
