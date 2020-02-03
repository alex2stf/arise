package com.arise.rapdroid;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;


import com.arise.astox.net.models.AbstractClient;
import com.arise.astox.net.models.http.HttpReader;
import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.astox.net.models.http.HttpResponseBuilder;
import com.arise.astox.net.models.http.HttpResponseReader;
import com.arise.core.tools.ThreadUtil;
import com.arise.core.tools.models.CompleteHandler;
import com.arise.core.tools.Mole;
import com.arise.core.tools.Util;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class BluetoothHttpClient extends AbstractClient<HttpRequest, HttpResponse, BluetoothSocket> {
    private final Mole log;
    private final BluetoothDevice mmDevice;
    private String deviceName;


    public BluetoothHttpClient(BluetoothDevice device){
        mmDevice = device;
        log = Mole.getInstance("B[" + device.getName() + "]");
        deviceName = device.getName();
    }

    @Override
    public BluetoothHttpClient setUuid(String uuid) {
        super.setUuid(uuid);
        return this;
    }





    volatile boolean checkingQueue = false;
    volatile boolean readInProgress = false;
    BluetoothSocket mmSocket = null;
    boolean firstInstance = false;
    @Override
    protected synchronized BluetoothSocket getConnection(HttpRequest request) throws Exception {
        request.addHeader("Correlation-Id", UUID.randomUUID().toString());
        if (mmSocket == null){
            log.info("createRfcommSocketToServiceRecord");
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString(getUuid()));
        }



        if (!mmSocket.isConnected()){
            try {
                mmSocket.connect();
            } catch (Exception e) {
                log.e("Socket connect method failed for device " + mmDevice.getName());
                mmSocket = null;
            }
        }

        return mmSocket;
    }




    @Override
    public void connect(HttpRequest request, CompleteHandler<BluetoothSocket> connectHandler) {
        ThreadUtil.fireAndForget(new Runnable() {
            @Override
            public void run() {
                try {
                    BluetoothSocket socket = getConnection(request);
                    log.info(mmDevice.getName(), "CONNECT complete");
                    connectHandler.onComplete(socket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void write(BluetoothSocket socket, HttpRequest request) {
        request.addHeader("Correlation-Id", UUID.randomUUID().toString());
        OutputStream out;
        try {
            out = socket.getOutputStream();
        } catch (IOException e) {
           log.error("Failed to fetch outputstream");
            httpResponseReader.flush();
            return;
        }
        try {
            out.write(request.getBytes());
        } catch (IOException e) {
            log.error("Failed to write to outputstream");
            return;
        }
        try {
            out.flush();
        } catch (IOException e) {
            log.error("Failed to write to flush");
            return;
        }

    }


    @Override
    public void sendAndReceive(HttpRequest request, CompleteHandler<HttpResponse> responseHandler) {
        synchronized (messageQueue){
            Message message = new Message();
            message.request = request;
            message.onComplete = responseHandler;
            messageQueue.add(message);
            log.info(mmDevice.getName() + "] queue add " + request);
            checkQueue();
        }
    }


    private synchronized void restartQueueCheck(){
        checkingQueue = false;
        checkQueue();
    }


    private synchronized void checkQueue() {

        if (checkingQueue){
            log.info( "already checking ");
            return;
        }
        if (readInProgress) {
            log.info( "read in progress");
            return;
        }
        if (messageQueue.isEmpty()){
            log.info("empty message queue");
            return;
        }

        if (currentMessage != null){
            new RuntimeException("WTF This should not happen").printStackTrace();
            return;
        }
        checkingQueue = true;

        try {
            currentMessage = messageQueue.take();
        } catch (InterruptedException e) {
            log.error("Failed to fetch message");
            restartQueueCheck();
            return;
        }
        if (currentMessage == null){
            restartQueueCheck();
            return;
        }

        connect(currentMessage.request, new CompleteHandler<BluetoothSocket>() {
            @Override
            public void onComplete(BluetoothSocket data) {

                if (data != null){
                    synchronized (data){
                        try {
                            log.info("writing request" + currentMessage.request.path());
                            data.getOutputStream().write(currentMessage.request.getBytes());
                        } catch (IOException e) {
                            log.error("Failed to write to device " + mmDevice.getName());
                            restartQueueCheck();
                            return;
                        }
                        read(data, currentMessage.onComplete);
                    }
                }
                else {
                    log.error("No socket instance found ");
                    restartQueueCheck();
                }
            }
        });
    }

    Message currentMessage;

    @Override
    public void send(HttpRequest request, CompleteHandler<BluetoothSocket> handler) {
        connect(request, handler);
        throw new RuntimeException("SHOULD NOT USE THIS!!!!");
    }


    class BluetoothReader extends HttpResponseReader {

        boolean handled = false;

        @Override
        public void handleRest(HttpReader reader) {
            handled = true;
            response.setBytes(bodyBytes.toByteArray());
            currentMessage.onComplete.onComplete(response);
            log.info("[" + mmDevice.getName() + "] received\n" + response);
            flush();
            currentMessage = null;
            readInProgress = false;
            restartQueueCheck();
        }

        @Override
        public synchronized void flush() {
            super.flush();
            readedBytes = 0;
            bodyBytes = new ByteArrayOutputStream();
            headersReaded = false;
            handled = false;
            log.info("Flushing " + mmDevice.getName());
        }


        @Override
        protected synchronized void onHeaderReadComplete(String text) {
            if (!text.startsWith("HTTP")){
                flush();
                readInProgress = false;
                return;
            }
            super.onHeaderReadComplete(text);
        }

        @Override
        public synchronized void readChar(byte b) {
            int contentLength = 0;

            if (headersReaded){
                contentLength = response.getContentLength();

                if (contentLength == 0){
                    flush();
                    throw new RuntimeException("EMPTY CONTENT LENGTH");

                }
                else if (bodyBytes.size() >= contentLength - 1 && !handled){
                    handleRest(this);
                    handled = true;
                    return;
                }

            }

            if (!handled) {
                super.readChar(b);
            }
//            System.out.println("readChar [" + (char)b + "] body size" + bodyBytes.size() + " contentLength= " + contentLength + " headersReaded=" + headersReaded + " handled=" + handled);
        }


    };

    BluetoothReader httpResponseReader = new BluetoothReader();

    @Override
    protected void read(BluetoothSocket socket, CompleteHandler<HttpResponse> responseHandler) {
        readLoop(socket);
    }



    private void readLoop(BluetoothSocket socket){
        byte[] buffer = new byte[256];
        int readed = 0;
        int start = 0;
        synchronized (httpResponseReader){
            httpResponseReader.flush();
            readInProgress = true;
            while (readInProgress) {
                try {
                    readed = socket.getInputStream().read(buffer);            //read bytes from input buffer
                    for (int i = start; i < readed ; i++){
                        httpResponseReader.readChar(buffer[i]);
                    };
                } catch (IOException e) {
                    log.error("Failed to read from device " + mmDevice.getName());
                    httpResponseReader.flush();
                    closeMmSocket(socket);
                    break;
                }
            }
        }
    }



    private void closeMmSocket(BluetoothSocket socket){
        if (socket == null){
            return;
        }
        try {
            socket.getInputStream().close();
        } catch (IOException ex) {
//                        ex.printStackTrace();
        }

        try {
            socket.getOutputStream().close();
        } catch (IOException ex) {
//                        ex.printStackTrace();
        }

        try {
            socket.close();
        } catch (IOException ex) {
//                        ex.printStackTrace();
        }




        mmSocket = null;
    }



    @Override
    public String getId() {
        String connect = getConnectionPath();
        return "" + connect + mmDevice.getName() + mmDevice.getAddress();
    }

    BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();

    class Message {
        HttpRequest request;
        CompleteHandler<HttpResponse> onComplete;
    }
}
