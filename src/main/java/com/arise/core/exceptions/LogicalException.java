package com.arise.core.exceptions;

public class LogicalException extends RuntimeException {
    public LogicalException(String message, Throwable cause){
        super(message);
        initCause(cause);
    }

    public LogicalException(String message){
        super(message);
    }

    public LogicalException(String message, String path, int lineNo){
        super(message + " at [" + path + "  - " + lineNo + "]");
    }


}
