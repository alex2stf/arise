package com.arise.astox.net.models;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by alex on 02/10/2017.
 */
public abstract class ServerResponse  {

    public void onServerError(IOException e) {

    }

    @Deprecated
    public boolean isSelfManageable() {
        return false;
    }

    public byte[] bytes(){
        return null;
    }

    public void onOutputStreamAccepted(ServerRequest serverRequest, OutputStream outputStream) {
        throw new ServerResponse.InvalidImplementation("onOutputStreamAccepted method should be @Override");
    }

    public void setServerName(String name) {
    }

    protected static class InvalidImplementation extends RuntimeException {
        public InvalidImplementation(String msg){
            super(msg);
        }
    }
}
