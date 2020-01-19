package com.arise.astox.net.models;

import com.arise.core.tools.Mole;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.ThreadUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

import static com.arise.core.tools.Util.close;

public abstract class StreamedServer<CONNECTION_PROVIDER, CONNECTION> extends AbstractServer<InputStream> {

    private static final Mole log = new Mole(StreamedServer.class);
    private final Class<CONNECTION> connectionClass;

    protected abstract CONNECTION_PROVIDER buildConnectionProvider();
    protected abstract CONNECTION acceptConnection(CONNECTION_PROVIDER provider) throws Exception;


    public StreamedServer(Class<CONNECTION> connectionClass){
        this.connectionClass = connectionClass;
    }

    @Override
    public void registerMessage(ServerMessage message) {
        CONNECTION connection = message.getArg(connectionClass);
        try {
            getOutputStream(connection).write(message.bytes());
        } catch (IOException e) {
            fireError(e);
        }
    }


    public void write(byte[] bytes, ConnectionSolver connectionSolver, WriteCompleteEvent event){
        CONNECTION connection = connectionSolver.getArg(connectionClass);
        try {
            getOutputStream(connection).write(bytes);
            event.onComplete();
        } catch (IOException e) {
            event.onError(e);
            fireError(e);
        }
    }



    public void start() throws Exception {
        super.start();
        CONNECTION_PROVIDER provider = buildConnectionProvider();
        firePostInit();
        while (running){
            CONNECTION connection = null;
            try {
                connection = acceptConnection(provider);
            } catch (Exception ex){
                fireError(ex);
            } finally {
                if (connection != null){
                    final CONNECTION finalConnection = connection;
                    ThreadUtil.fireAndForget(new Runnable() {
                        @Override
                        public void run() {
                            handle(finalConnection);
                        }
                    });
                }
            }
        }
        close(provider);
    }



    protected void handle(final CONNECTION connection){
        try {


            InputStream inputStream = getInputStream(connection);


            readPayload(inputStream, new ReadCompleteHandler<ServerRequest>() {
                @Override
                public void onReadComplete(ServerRequest serverRequest) {

                    System.out.println("RECEIVED " + serverRequest);
                    if (serverRequest == null){
                        log.warn("NULL server request");
                        return;
                    }
                    if (!requestHandler.validate(serverRequest)){
                        close(connection);
                        return;
                    }

                    DuplexDraft draft = requestToDuplex(serverRequest);

                    OutputStream outputStream = getOutputStream(connection);

                    try {
                        if (draft != null){
                            handleDuplex(connection, draft, serverRequest, outputStream);
                        }
                        else {
                            if (outputStream == null){
                                log.error("Nothing to write into a null outputstream");
                                close(connection);
                                return;
                            }
                            handleNonDuplex(serverRequest, connection, outputStream);
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                }
            });



        } catch (Exception ex){
            fireError(ex);
        }
    }




    private void handleDuplex(CONNECTION connection, DuplexDraft draft, ServerRequest serverRequest, OutputStream outputStream) throws IOException {
        ServerResponse serverResponse = getDuplexHandshakeResponse(draft, serverRequest);
        if (serverResponse == null){
            close(connection);
            return;
        }

        System.out.println("E DRAFT REQUEST, return " + serverResponse);
        serverResponse.setServerName(getName());
        byte[] response = serverResponse.bytes();
        outputStream.write(response, 0, response.length);


        DuplexDraft.Connection duplexConnection = draft.createConnection(this, connection, null, null);
        requestHandler.onDuplexConnect(this, serverRequest, duplexConnection);

        DuplexDraft.DuplexInputStream duplexStream = draft.buildInputStream(getInputStream(connection));


        boolean allowWsRead = true;

        while (allowWsRead){
            // Receive a frame from the server.
            DuplexDraft.Frame frame = null;
            try {
                frame = duplexStream.readFrame();
            } catch (Exception ex){
                allowWsRead = false;
                fireError(ex);
            } finally {
                if (frame != null) {
                    requestHandler.onFrame(frame, duplexConnection);
                }
                if (!allowWsRead || frame.isCloseFrame()){
                    onDuplexClose(duplexConnection);
                    close(duplexStream);
                    close(connection);
                    break;
                }
            }
        }
    }


    protected void handleNonDuplex(ServerRequest serverRequest, CONNECTION connection, OutputStream outputStream) throws IOException {
        ServerResponse response;
        try {
            response = requestHandler.getResponse(this, serverRequest);
        } catch (Exception ex){
            ex.printStackTrace();
            response = requestHandler.getDefaultResponse(this);
        }

        System.out.println("RESPONSE " + response);
        if (response != null){
            response.setServerName(getName());
            if (response.isSelfManageable()){
                response.onTransporterAccepted(serverRequest, this, connection);
            } else {
               try {
                   outputStream.write(response.bytes());
               } catch (SocketException e){
                   if (e.getMessage().contains("closed by remote host")){
                       log.warn(e.getMessage());
                       close(connection);
                   }

                   else {
                       throw e;
                   }
               } catch (Exception e){
                   if ( (StringUtil.hasContent(e.getMessage())) && e.getMessage().contains("connection was aborted by the software in your host machine"))
                   {
                       log.warn(e.getMessage());
                       close(connection);
                   }
                    else
                   {
                       throw e;
                   }
               }
               close(connection);
            }
        } else {
            outputStream.write(requestHandler.getDefaultResponse(this).bytes());
            close(connection);
        }
    }

    @Override
    protected void solveInterceptor(ServerRequestBuilder builder, InputStream data, ReadCompleteHandler<ServerRequest> completeHandler) {
        builder.readInputStream(data, completeHandler);
    }


    //    @Override
//    protected ServerRequest extractSingleRequestBeforeValidation(ServerRequestBuilder builder, InputStream data) throws Exception {
//        return builder.readHeader(data);
//    }
//
//    @Override
//    protected void solveSingleRequestAfterValidation(ServerRequest request, ServerRequestBuilder builder, InputStream data) throws Exception {
//        builder.solveRequestAfterValidation(request, data);
//    }


    protected abstract InputStream getInputStream(CONNECTION connection);
    protected abstract OutputStream getOutputStream(CONNECTION connection);
}
