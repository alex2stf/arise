package com.astox.rapdroid;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.arise.astox.net.http.HttpRequest;
import com.arise.astox.net.http.HttpResponse;
import com.arise.astox.net.http.HttpResponseBuilder;
import com.arise.astox.net.models.AbstractClient;
import com.arise.core.tools.Mole;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static com.arise.core.tools.Util.close;

public class BluetoothHttpClient extends AbstractClient<HttpRequest, HttpResponse, BluetoothSocket> {
    private static final Mole log = Mole.getInstance(BluetoothHttpClient.class);
    private final BluetoothDevice mmDevice;
    private volatile BluetoothSocket mmSocket = null;

    public BluetoothHttpClient(BluetoothDevice device){
        mmDevice = device;
        this.setBuilder(new HttpResponseBuilder());
    }

    @Override
    public BluetoothHttpClient setUuid(String uuid) {
        super.setUuid(uuid);
        return this;
    }

    @Override
    protected BluetoothSocket getConnection() throws Exception {
        if (mmSocket == null) {
            try {
                mmSocket = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString(getUuid()));
                mmSocket.connect();
            } catch (IOException e) {
                log.e("Socket's create() method failed", e);
                close(mmSocket);
                return null;
            }
        }
        return mmSocket;
    }

    @Override
    protected OutputStream getOutputStream(BluetoothSocket bluetoothSocket) {
        try {
            return bluetoothSocket.getOutputStream();
        } catch (IOException e) {
            this.onError(e);
        }
        return null;
    }

    @Override
    protected InputStream getInputStream(BluetoothSocket bluetoothSocket) {
        try {
            return bluetoothSocket.getInputStream();
        } catch (IOException e) {
            onError(e);
        }
        return null;
    }


}
