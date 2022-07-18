package com.arise.astox.net.servers.nio;

import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.models.ConnectionSolver;
import com.arise.astox.net.models.DuplexDraft;
import com.arise.astox.net.models.ServerMessage;
import com.arise.astox.net.models.ServerRequest;
import com.arise.astox.net.models.ServerResponse;
import com.arise.astox.net.servers.draft_6455.WebSocketException;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.ThreadUtil;
import com.arise.core.tools.Util;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

import static com.arise.astox.net.servers.nio.NIOChannelData.Type.CLOSEABLE;
import static com.arise.astox.net.servers.nio.NIOChannelData.Type.DUPLEX;
import static com.arise.astox.net.servers.nio.NioSslPeer.closeConnection;
import static com.arise.astox.net.servers.nio.NioSslPeer.doHandshake;
import static com.arise.astox.net.servers.nio.NioSslPeer.enlargeApplicationBuffer;
import static com.arise.astox.net.servers.nio.NioSslPeer.enlargePacketBuffer;
import static com.arise.astox.net.servers.nio.NioSslPeer.handleBufferUnderflow;
import static com.arise.astox.net.servers.nio.NioSslPeer.handleEndOfStream;


/**
 * An SSL/TLS server, that will listen to a specific address and port and serve SSL/TLS connections
 * compatible with the protocol it applies.
 * <p/>
 * After initialization {@link NIOServer#start()} should be called so the server starts to listen to
 * new connection requests. At this point, start is blocking, so, in order to be able to gracefully stopPreviews
 * the server, a {@link Runnable} containing a server object should be created. This runnable should 
 * start the server in its run method and also provide a stopPreviews method, which will call {@link NIOServer#stop()}.
 * </p>
 * NIOServer makes use of Java NIO, and specifically listens to new connection requests with a {@link ServerSocketChannel}, which will
 * create new {@link SocketChannel}s and a {@link Selector} which serves all the connections in one thread.
 *
 * @author <a href="mailto:alex.a.karnezis@gmail.com">Alex Karnezis</a>
 */
public class NIOServer extends AbstractServer<SocketChannel> {
    protected static final Mole log = Mole.getLogger(NioSslPeer.class);

	/**
	 * Declares if the server is active to serve and create new connections.
	 */
	private boolean active;

    /**
     * A part of Java NIO that will be used to serve all connections to the server in one thread.
     */
    private Selector selector;


    @Override
    public void write(byte[] bytes, ConnectionSolver c, final WriteCompleteEvent event) {
        this.write(c.socketChannel(), c.sslEngine(), c.selectionKey(), bytes, new Handler() {
            @Override
            public void onSuccess() {
                event.onComplete();
            }

            @Override
            public void onError(SocketChannel socketChannel, SSLEngine engine, SelectionKey key, NIOServer server, Exception ex) {
                super.onError(socketChannel, engine, key, server, ex);
                event.onError(ex);
            }
        });
    }

    /**
     * Should be called in order the server to start listening to new connections.
     * This method will run in a loop as long as the server is active. In order to stopPreviews the server
     * you should use {@link NIOServer#stop()} which will set it to inactive state
     * and also wake up the listener, which may be in blocking select() state.
     *
     * @throws Exception
     */
    public void start() throws Exception {

        System.out.println("NIO START");


        selector = SelectorProvider.provider().openSelector();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);

        if (getHost() != null) {
            serverSocketChannel.socket().bind(new InetSocketAddress(getHost(), getPort()));
        } else {
            serverSocketChannel.socket().bind(new InetSocketAddress(getPort()));
        }
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        active = true;

    	log.debug("Initialized and waiting for new connections...");
        firePostInit();


        while (isActive()) {
            selector.select();
            Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
            while (selectedKeys.hasNext()) {
                SelectionKey key = selectedKeys.next();
                selectedKeys.remove();
                if (!key.isValid()) {
                    continue;
                }
                if (key.isAcceptable()) {
                    accept(key);
                }
                else if(key.isReadable()){
                    read(key);
                }
                else if (key.isWritable()) {
                    write(key);
                }

                iterateThroughMessages();
            }
            System.out.println("loop, wait for keys isActive() " + isActive());
        }


        
        log.debug("Goodbye!");
    }




    private void iterateThroughMessages() throws InterruptedException {

        while (!messagesToWrite.isEmpty()){
            ServerMessage message = null;

            message = messagesToWrite.take();

            final SocketChannel socketChannel = message.socketChannel();
            final SelectionKey key = message.selectionKey();
            final NIOChannelData channelData = (NIOChannelData) key.attachment();

            write(socketChannel, message.sslEngine(), key, message.bytes(), new Handler() {
                @Override
                public void onSuccess() {
                try {
                    socketChannel.register(key.selector(), SelectionKey.OP_READ|SelectionKey.OP_WRITE, channelData);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
                }
            });
        }
    }
    
    /**
     * Sets the server to an inactive state, in order to exit the reading loop in {@link NIOServer#start()}
     * and also wakes up the selector, which may be in select() blocking state.
     */
    public void stop() {
    	log.debug("Will now close server...");
    	active = false;
    	NioSslPeer.executor.shutdown();
    	selector.wakeup();
    }



    @Override
    public void registerMessage(ServerMessage message) {
        try {
            messagesToWrite.put(message);
        } catch (InterruptedException e) {
            fireError(e);
        }
    }

//    @Override
//    protected void solveInterceptor(ServerRequestBuilder builder, SocketChannel socketChannel, ReadCompleteHandler<ServerRequest> handler) {
//            builder.readSocketChannel(socketChannel, handler);
//    }



    /**
     * Will be called after a new connection request arrives to the server. Creates the {@link SocketChannel} that will
     * be used as the network layer link, and the {@link SSLEngine} that will encrypt and decrypt all the data
     * that will be exchanged during the session with this specific client.
     *
     * @param key - the key dedicated to the {@link ServerSocketChannel} used by the server to listen to new connection requests.
     * @throws Exception
     */
    private void accept(final SelectionKey key) throws Exception {
        final SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
        socketChannel.configureBlocking(false);
        log.debug("New connection request from " + StringUtil.dump(socketChannel));
        //SSL block
        if (isSecure()){
            SSLEngine engine = sslContext.createSSLEngine();
            engine.setUseClientMode(false);
            engine.beginHandshake();

            doHandshake(socketChannel, engine, key, this, new com.arise.core.models.Handler<ServerRequest>() {
                @Override
                public void handle(ServerRequest data) {
                    if (data == null){
                        Util.close(socketChannel);
                        return;
                    }
                    try {
                        SelectionKey result = socketChannel.register(key.selector(), SelectionKey.OP_READ, key.attachment());
                        read(result);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        else {
            socketChannel.register(key.selector(), SelectionKey.OP_READ, new NIOChannelData(CLOSEABLE, null));
        }

    }







    protected void write(final SelectionKey key) throws IOException {
        final NIOChannelData channelData = (NIOChannelData) key.attachment();
        final SocketChannel socketChannel = (SocketChannel) key.channel();
        final SSLEngine engine = channelData.getSslEngine();
        byte [] bytes = null;

        ServerResponse response = channelData.getResponse();
        System.out.println("RESPONSE: ");

        if (channelData.isCloseable()){
            System.out.println("WRITE HTTP TO " + StringUtil.dump(socketChannel));
            if (response == null){
                response = requestHandler.getResponse(this, channelData.getRequest());
            }
            response.setServerName(getName()); //TODO add more server info
            if (response.isSelfManageable()){
                final ServerResponse finalResponse = response;
                final NIOServer self = this;
                ThreadUtil.fireAndForget(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            finalResponse.onTransporterAccepted(channelData.getRequest(), key, socketChannel, engine, self);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }, "NIOSelfManagedThread-" + StringUtil.dump(socketChannel));

            } else {
                bytes = response.bytes();
                write(socketChannel, engine, key, bytes, new Handler() {
                    @Override
                    public void onSuccess() {
                        log.trace("Http connection closed for " + socketChannel.socket().getRemoteSocketAddress());
                        closeHttpConnection(key, socketChannel, engine);
                    }
                });
            }
        }
        else if (channelData.getDuplexState().equals(NIOChannelData.DuplexState.CONNECTING)){
            bytes = response.bytes();

            write(socketChannel, engine, key, bytes, new Handler() {
                @Override
                public void onSuccess() {
                    System.out.println("LEAVING "+ StringUtil.dump(socketChannel) + " OPENED FOR DUPLEX COMMUNICATION, SWITCH TO READ|WRITE");
                    channelData.setDuplexState(NIOChannelData.DuplexState.ALIVE);
                    try {
                        socketChannel.register(key.selector(), SelectionKey.OP_READ|SelectionKey.OP_WRITE, channelData);
                    } catch (ClosedChannelException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }



    protected void read(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        NIOChannelData channelData = (NIOChannelData) key.attachment();
        SSLEngine engine = channelData.getSslEngine();

        if (isSecure()){
            try {
                readSecure(key, socketChannel, channelData, engine, this);
            } catch (Exception e) {
                fireError(e);
            }
        }
        else {
            try {
                readStandard(channelData, socketChannel, key);
            } catch (Exception e) {
                fireError(e);
            }
        }
    }
    public static final int RCVBUF = 16384;

    private void readStandard(final NIOChannelData channelData, final SocketChannel socketChannel, final SelectionKey key) throws Exception {
        if (channelData.isCloseable()){

            this.serverRequestBuilder.readSocketChannel(socketChannel, new com.arise.core.models.Handler<ServerRequest>() {
                @Override
                public void handle(ServerRequest serverRequest) {
                    if (serverRequest == null || !requestHandler.validate(serverRequest)) {
                        Util.close(socketChannel);
                        key.cancel();
                        return;
                    }

                    channelData.setRequest(serverRequest);
                    try {
                        postRead(channelData, socketChannel, null, null, key);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (WebSocketException e) {
                        e.printStackTrace();
                    }
                }
            }, new com.arise.core.models.Handler<Throwable>() {
                @Override
                public void handle(Throwable data) {
                    data.printStackTrace();
                    Util.close(socketChannel);
                }
            });



        } else { //DUPLEX
            int bulkLimit = RCVBUF;
            int read;
            NIOBytesSolver nioBytesSolver = new NIOBytesSolver();
            ByteBuffer buff;
            do {
                buff = ByteBuffer.allocate(bulkLimit);
                read = socketChannel.read(buff);
                if (read > 0){
                   nioBytesSolver.add(buff);
                }
            } while (read > 0);

            postRead(channelData, socketChannel, nioBytesSolver.getAll(), null, key);
        }
    }


    private static void readSecure(SelectionKey key, SocketChannel socketChannel,
                                   NIOChannelData channelData, SSLEngine engine,
                                   NIOServer sslServer) throws IOException, WebSocketException {

        ByteBuffer peerNetData = ByteBuffer.allocate(engine.getSession().getPacketBufferSize());
        ByteBuffer peerAppData = ByteBuffer.allocate(engine.getSession().getApplicationBufferSize());

        NIOBytesSolver nioBytesSolver = new NIOBytesSolver();

        boolean readSuccess = false;
        peerNetData.clear();
        int bytesRead = socketChannel.read(peerNetData);

        int x = 0;
        if (bytesRead > 0) {
            peerNetData.flip();
            while (peerNetData.hasRemaining()) {
                peerAppData.clear();
                SSLEngineResult result = engine.unwrap(peerNetData, peerAppData);
                switch (result.getStatus()) {
                    case OK:

                        System.out.println(x + ")" + (peerAppData.capacity() - peerAppData.remaining()) + "] READED " +peerAppData.array().length + ")" +  new String( peerAppData.array()));
                        nioBytesSolver.add(peerAppData);
                        peerAppData.clear();
                        readSuccess = true;
                        x++;
                        break;
                    case BUFFER_OVERFLOW:
                        peerAppData = enlargeApplicationBuffer(engine, peerAppData);
                        break;
                    case BUFFER_UNDERFLOW:
                        peerNetData = handleBufferUnderflow(engine, peerNetData);
                        break;
                    case CLOSED:
                        log.debug("WelandAPI wants to close connection...");
                        closeConnection(socketChannel, engine, key, sslServer);
                        log.debug("Goodbye client!");
                        return;
                    default:
                        throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                }
            }
        } else if (bytesRead < 0) {
            log.error("Received DEend of stream. Will try to close connection with client...");
            handleEndOfStream(socketChannel, engine, key, sslServer);
            log.debug("Goodbye client!");
        }

        if (readSuccess || bytesRead == 0){
            nioBytesSolver.add(peerAppData);
            sslServer.postReadSSL(channelData, nioBytesSolver.getAll(), key, socketChannel, engine);
        }
    }

    private void postReadSSL(final NIOChannelData channelData, final byte[] finallyBytes, final SelectionKey key, final SocketChannel socketChannel, final SSLEngine engine) throws IOException, WebSocketException {
        if (channelData.isCloseable()){
            ByteBuffer buffer = ByteBuffer.allocate(finallyBytes.length);
            buffer.put(finallyBytes);
            parseByteBuffer(buffer, new com.arise.core.models.Handler<ServerRequest>() {
                @Override
                public void handle(ServerRequest request) {
                    System.out.println("SSL READ COMPLETE");
                    if (request == null){
                        closeHttpConnection(key, socketChannel, engine);
                        return;
                    }
                    channelData.setRequest(request);
                    try {
                        postRead(channelData, socketChannel, finallyBytes, engine, key);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (WebSocketException e) {
                        e.printStackTrace();
                    }
                }
            });

        } else {
            postRead(channelData, socketChannel, finallyBytes, engine, key);
        }

    }


    public ServerRequest parseByteBuffer(Object input, com.arise.core.models.Handler<ServerRequest> onSuccess) {
        this.serverRequestBuilder.readByteBuffer((ByteBuffer) input, onSuccess, new com.arise.core.models.Handler<Throwable>() {
            @Override
            public void handle(Throwable data) {
                data.printStackTrace();
            }
        });
        return null;
    }


    private ServerResponse fixChannel(NIOChannelData channelData, SocketChannel socketChannel, SSLEngine engine, SelectionKey key){
        log.info(" fixChannel " + StringUtil.dump(socketChannel));

        ServerResponse response = null;
        ServerRequest request = channelData.getRequest();
        if (channelData.getDuplexState().equals(NIOChannelData.DuplexState.UNSET)){
            DuplexDraft duplexDraft = this.requestToDuplex(request);
            if (duplexDraft != null){
                //handshake response
                response = this.getDuplexHandshakeResponse(duplexDraft, request);

                //TODO validate response!!!
                channelData.setType(DUPLEX);
                channelData.setDuplexDraft(duplexDraft);
                DuplexDraft.Connection connection = duplexDraft.createConnection(this, socketChannel, engine, key);
                channelData.setDuplexConnection(connection);
                channelData.setDuplexState(NIOChannelData.DuplexState.CONNECTING);
                requestHandler.onDuplexConnect(this, request, connection);
                log.trace("IS DUPLEX: " + StringUtil.dump(socketChannel));
            }
        }


        //daca exista request si channel nu e duplex
        if (request != null && !channelData.getDuplexState().equals(NIOChannelData.DuplexState.CONNECTING)){
            response = requestHandler.getResponse(this, channelData.getRequest());
        }
        else if(!channelData.getDuplexState().equals(NIOChannelData.DuplexState.CONNECTING)){
            response = requestHandler.getExceptionResponse(this, null);
        }
        channelData.setResponse(response);

        return response;
    }




    private void postRead(NIOChannelData channelData, final SocketChannel socketChannel, byte[] readedBytes, final SSLEngine engine, final SelectionKey key) throws IOException, WebSocketException {

        if (!channelData.getDuplexState().equals(NIOChannelData.DuplexState.ALIVE)){
            fixChannel(channelData, socketChannel, engine, key);
        }

        //fa write ca sa trimiti respunsuri http:
        if (channelData.isCloseable() || channelData.getDuplexState().equals(NIOChannelData.DuplexState.CONNECTING)){
            log.info("WRITE HTTP RESPONSE TO " + StringUtil.dump(socketChannel));
            write(key);
            return;
        }


        log.info( "DUPLEX " + readedBytes.length + " bites read from: " + StringUtil.dump(socketChannel) + new String(readedBytes));

        DuplexDraft.Connection connection = channelData.getDuplexConnection();
        channelData.getDuplexDraft().parseBytes(readedBytes, connection, new DuplexDraft.ParseEvent() {
            @Override
            public void onFrameFound(DuplexDraft.Frame frame, DuplexDraft.Connection connection) {
                if (frame.isCloseFrame()){
                    onDuplexClose(connection);
                    closeHttpConnection(key, socketChannel, engine);
                } else {
                    handleFrame(frame, connection);
                }
            }

            @Override
            public void onError(Throwable err) {
                fireError(err);
            }
        });
    }


    private void handleFrame(DuplexDraft.Frame frame, DuplexDraft.Connection connection){
          try {
              requestHandler.onFrame(frame, connection);
          } catch (Throwable err){
              fireError(err);
          }
    }


    public void write(SocketChannel socketChannel, SSLEngine engine, SelectionKey key, byte [] bytes, Handler onComplete)  {
        final NIOServer self = this;
        if (isSecure()){
            try {
                writeSecure(socketChannel, engine, key, bytes,  onComplete);
            } catch (IOException e) {
                onComplete.onError(socketChannel, engine, key, self, e);
            }
        } else {
            try {
                socketChannel.write(ByteBuffer.wrap(bytes));
                onComplete.onSuccess();
            } catch (Exception e) {
                onComplete.onError(socketChannel, engine, key, self, e);
            }
        }
    }


    private void writeSecure(SocketChannel socketChannel, SSLEngine engine, SelectionKey key, byte [] bytes, Handler onComplete) throws IOException {

        ByteBuffer myAppData = ByteBuffer.allocate(engine.getSession().getApplicationBufferSize());
        ByteBuffer myNetData = ByteBuffer.allocate(engine.getSession().getPacketBufferSize());


        myAppData.put(bytes);
        myAppData.flip();

        while (myAppData.hasRemaining()) {
            // The loop has a meaning for (outgoing) messages larger than 16KB.
            // Every wrap call will remove 16KB from the original message and sendSync it to the remote peer.
            myNetData.clear();
            SSLEngineResult result = engine.wrap(myAppData, myNetData);
            switch (result.getStatus()) {
                case OK:
                    myNetData.flip();
                    while (myNetData.hasRemaining()) {
                        try {
                            socketChannel.write(myNetData);
                        } catch (Exception ex){
                            fireError(ex);
                        }
                    }
                    break;
                case BUFFER_OVERFLOW:
                    myNetData = enlargePacketBuffer(engine, myNetData);
                    break;
                case BUFFER_UNDERFLOW:
                    throw new SSLException("Buffer underflow occured after a wrap. I don't think we should ever get here.");
                case CLOSED:
                    closeConnection(socketChannel, engine, key, this);
                    return;
                default:
                    fireError(new IllegalStateException("Invalid SSL status: " + result.getStatus()));
            }
        }
        onComplete.onSuccess();
    }

    public void closeHttpConnection(SelectionKey key, SocketChannel socketChannel, SSLEngine engine) {
        closeConnection(socketChannel, engine, key, this);
        key.cancel();
    }

    /**
     * Determines if the the server is active or not.
     *
     * @return if the server is active or not.
     */
    private boolean isActive() {
        return active;
    }

    @Deprecated
    public static abstract class Handler {
        public abstract void onSuccess();
        public void onError(SocketChannel socketChannel, SSLEngine engine, SelectionKey key, NIOServer server, Exception ex){
            if (! (ex instanceof ClosedChannelException)) {
                server.closeHttpConnection(key, socketChannel, engine);
            }
        }
    }

}
