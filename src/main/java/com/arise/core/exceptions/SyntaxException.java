package com.arise.core.exceptions;

public class SyntaxException extends RuntimeException {
    public SyntaxException(String message, String path, int line){
        super(message + " at " + path + " " + line);
    }

    public SyntaxException(String message, Throwable cause){
        super(message);
        initCause(cause);
    }

    public SyntaxException(String message){
        super(message);
    }
}
