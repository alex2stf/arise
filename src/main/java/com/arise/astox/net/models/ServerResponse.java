package com.arise.astox.net.models;

import java.io.IOException;

/**
 * Created by alex on 02/10/2017.
 */
public abstract class ServerResponse  {

    public void onServerError(IOException e) {

    }

    public boolean isSelfManageable() {
        return false;
    }

    public byte[] bytes(){
        return null;
    }

    public void onTransporterAccepted(ServerRequest serverRequest, Object... transporters) {
        throw new ServerResponse.InvalidImplementation("onTransporterAccepted method should be @Override");
    }

    public void setServerName(String name) {
    }

    protected static class InvalidImplementation extends RuntimeException {
        public InvalidImplementation(String msg){
            super(msg);
        }
    }
}
