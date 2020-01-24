package com.arise.rapdroid;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;


import com.arise.astox.net.models.AbstractClient;
import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.astox.net.models.http.HttpResponseBuilder;
import com.arise.core.tools.models.CompleteHandler;
import com.arise.core.tools.Mole;
import com.arise.core.tools.ThreadUtil;
import com.arise.core.tools.Util;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothHttpClient extends AbstractClient<HttpRequest, HttpResponse, BluetoothSocket> {
    private static final Mole log = Mole.getInstance(BluetoothHttpClient.class);
    private final BluetoothDevice mmDevice;


    public BluetoothHttpClient(BluetoothDevice device){
        mmDevice = device;
    }

    @Override
    public BluetoothHttpClient setUuid(String uuid) {
        super.setUuid(uuid);
        return this;
    }






    @Override
    protected synchronized BluetoothSocket getConnection(HttpRequest request) throws Exception {
        BluetoothSocket mmSocket = null;


        mmSocket = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString(getUuid()));
        try {
            mmSocket.connect();
        } catch (Exception e) {
            log.e("Socket's create() method failed", e);
            Util.close(mmSocket);
            onError(e);
            return mmSocket;
        }

        return mmSocket;
    }

    @Override
    protected void write(BluetoothSocket socket, HttpRequest request) {
        synchronized (socket){
            try {
                socket.getOutputStream().write(request.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void read(BluetoothSocket socket, CompleteHandler<HttpResponse> responseHandler) {
            synchronized (socket){
                try {
                    InputStream inputStream  = socket.getInputStream();
                    httpResponseBuilder.readInputStream(inputStream, new CompleteHandler<HttpResponse>() {
                        @Override
                        public void onComplete(HttpResponse data) {
                            //first dispatch event, then close streams
                            responseHandler.onComplete(data);

                            try {
                                inputStream.close();
                                socket.getOutputStream().close();
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

    }



    HttpResponseBuilder httpResponseBuilder = new HttpResponseBuilder();






}
