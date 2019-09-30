package com.arise.astox.net.exceptions;

/**
 * Created by alex on 02/10/2017.
 */
public class ServiceException extends RuntimeException {
    public ServiceException(String m){
        super(m);
    }
    public ServiceException(String m, Throwable c){
        super(m, c);
    }
}
