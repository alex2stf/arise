package com.arise.core.exceptions;

public class DependencyException extends RuntimeException {

    public DependencyException(String message, Throwable cause){
        super(message);
        initCause(cause);
    }

    public DependencyException(String message){
        super(message);
    }

    public DependencyException(String message, String path, int lineNo){
        super(message + " at [" + path + "  - " + lineNo + "]");
    }
}
