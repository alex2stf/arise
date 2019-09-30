package com.arise.astox.net.models;

import com.arise.astox.net.http.HttpResponse;
import com.arise.astox.net.models.AbstractServer.WriteCompleteEvent;
import com.arise.core.tools.Mole;

import java.util.HashSet;
import java.util.Set;


public class SelfManagedResponse extends HttpResponse {

    private static final Mole log = Mole.getInstance(SelfManagedResponse.class);

    @Override
    public boolean isSelfManageable() {
        return true;
    }

    private Set<Connection> connectionSet = new HashSet<>();

    private volatile boolean setInModify = false;

    @Override
    public synchronized void onTransporterAccepted(ServerRequest serverRequest, Object... args) {
        setInModify = true;
        connectionSet.add(new Connection(this, args));
        setInModify = false;
    }

    public synchronized void sendHeader(byte[] bytes){
        for (Connection connection : connectionSet){
            if (!connection.headerSent){
                if(connection.sendSync(bytes)){
                    connection.headerSent = true;
                }
                else {
                    remove(connection);
                }
            }
        }
    }

    public void sendSync(byte[] bytes){
        if (setInModify){
            return;
        }
        for (Connection connection : connectionSet) {
            if (connection.headerSent) {
                if(!connection.sendSync(bytes)){
                    remove(connection);
                }
            }
        }
    }

    private void remove(Connection connection) {
        connection.close();
        synchronized (this) {
            setInModify = true;
            connectionSet.remove(connection);
            setInModify = false;
        }
    }

    private void removeWithoutClose(Connection connection) {
        connectionSet.remove(connection);
    }


    public void send(byte[] bytes){
        if (setInModify){
            return;
        }
        for (Connection connection : connectionSet) {
            if (connection.headerSent) {
                if(!connection.sendAsync(bytes)){
                    remove(connection);
                }
            }
        }
    }


    public void closeConnections() {
       synchronized (this){
           setInModify = true;
           for (Connection t: connectionSet){
               t.close();
           }
           setInModify = false;
       }
    }

    public static class Connection extends ConnectionSolver {


        volatile boolean headerSent = false;

        final SelfManagedResponse parent;

        public Connection(SelfManagedResponse parent, Object ... args){
            super(args);
            this.parent = parent;
        }

        private volatile boolean isWriting = false;

        public boolean sendSync(final byte[] bytes) {
            if (!isWriting) {
                synchronized (this) {
                    isWriting = true;
                    server().write(bytes, this, new WriteCompleteEvent() {
                        @Override
                        public void onComplete() {
                            isWriting = false;
                        }
                    });
                }
            } else {
                log.info("SKIP BYTES IN WRITING STATE");
            }
            return true;
        }





        @Override
        public int hashCode() {
            return toString().hashCode();
        }

        public void close() {
            synchronized (this) {
                closeStreamables();
                log.info("closing " + this);
                parent.removeWithoutClose(this);
            }
        }

        public boolean sendAsync(final byte[] bytes) {
            server().write(bytes, this, new WriteCompleteEvent() {
                @Override
                public void onComplete() {
                    isWriting = false;
                }
            });
            return true;
        }
    }


}
