package com.arise.astox.net.servers.io;


import com.arise.astox.net.models.StreamedServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * Created by alex on 12/21/2017.
 */
public class IOServer extends StreamedServer<ServerSocket, Socket> {

    public IOServer() {
        super(Socket.class);
    }

    @Override
    protected ServerSocket buildConnectionProvider() {
        try {
            //TODO make InetAddress part of AbstractServer + better socket builder
            if (sslContext != null){
                SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
                if (getHost() != null){
                    InetAddress serverAddr = InetAddress.getByName(getHost());
                    return sslServerSocketFactory.createServerSocket(getPort(), 20, serverAddr);
                } else {
                    return sslServerSocketFactory.createServerSocket(getPort());
                }
            } else {
                if (getHost() != null){
                    InetAddress serverAddr = InetAddress.getByName(getHost());
                    return new ServerSocket(getPort(), 20, serverAddr);
                } else {
                    return new ServerSocket(getPort());
                }
            }

        } catch (IOException e) {
            fireError(e);
        }
        return null;
    }

    @Override
    protected Socket acceptConnection(ServerSocket serverSocket) throws Exception {
        return serverSocket.accept();
    }




    @Override
    protected InputStream getInputStream(Socket socket) {
        try {
            return socket.getInputStream();
        } catch (IOException e) {
            fireError(e);
        }
        return null;
    }

    @Override
    protected OutputStream getOutputStream(Socket socket) {
        try {
            return socket.getOutputStream();
        } catch (IOException e) {
           fireError(e);
        }
        return null;
    }


}
