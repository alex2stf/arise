package com.arise.astox.net.models;

import com.arise.core.tools.Util;

import javax.net.ssl.SSLEngine;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Wrapper class to solve duplex/long pooled communication issues. It can hold  {@link SocketChannel}
 * {@link SSLEngine}, {@link SelectionKey}, {@link AbstractServer}, {@link Socket} or any further
 * stream based connections used by {@link StreamedServer}
 * @see {@link ServerMessage} and {@link StreamedServer#registerMessage(ServerMessage)}
 */
public class ConnectionSolver {

    private final Socket _socket;
    final SelectionKey _selectionKey;
    final SocketChannel _socketChannel;
    final SSLEngine _sslEngine;
    final AbstractServer _abstractServer;
    final String mode;

    private final Object[] args;


    public SocketChannel socketChannel(){
        return getArg(SocketChannel.class);
    }

    public SelectionKey selectionKey(){
        return getArg(SelectionKey.class);
    }

    public SSLEngine sslEngine(){
        return getArg(SSLEngine.class);
    }

    public AbstractServer server(){
        return getArg(AbstractServer.class);
    }


    public ConnectionSolver(Object ... args) {
        this.args = args;
        Socket socket = null;
        SelectionKey selectionKey = null;
        SocketChannel socketChannel = null;
        SSLEngine sslEngine = null;
        AbstractServer abstractServer = null;

        for (int i = 0; i < args.length; i++){
            if (args[i] == null){
                continue;
            }
            if (args[i] instanceof Socket){
                socket = (Socket) args[i];
            }
            else if (args[i] instanceof SelectionKey){
                selectionKey = (SelectionKey) args[i];
            }
            else if (args[i] instanceof SocketChannel){
                socketChannel = (SocketChannel) args[i];
            }
            else if (args[i] instanceof SSLEngine){
                sslEngine = (SSLEngine) args[i];
            }
            else if (args[i] instanceof AbstractServer){
                abstractServer = (AbstractServer) args[i];
            }
        }

        _socket = socket;
        _abstractServer = abstractServer;
        _selectionKey = selectionKey;
        _socketChannel = socketChannel;
        _sslEngine = sslEngine;

        if (_socket != null && (_selectionKey == null && _socketChannel == null) ){
            mode = "IO";
        }
        else if(_socket == null && _selectionKey != null && _socketChannel != null){
            mode = "NIO";
        }
        else {
            mode = "UNSET";
        }
    }

    public <T> T getArg(Class<T> type){
        for (Object o: args){
            if (o != null && ( o.getClass().equals(type) || type.isAssignableFrom(o.getClass())) ){
                return (T) o;
            }
        }
        return null;
    }




    @Override
    public String toString() {
        return mode + "]" + (_socket != null ? String.valueOf(_socket) : "") +
            (_socketChannel != null ? String.valueOf(_socketChannel) : "") +
            (_sslEngine != null ? String.valueOf(_sslEngine) : "") +
            (_selectionKey != null ? String.valueOf(_selectionKey) : "");
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public void closeStreamables() {
        Util.close(_socket);
    }

}
