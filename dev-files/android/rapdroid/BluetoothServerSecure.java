package com.arise.rapdroid;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import com.arise.astox.net.models.StreamedServer;
import com.arise.core.tools.Mole;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothServerSecure extends StreamedServer<BluetoothServerSocket, BluetoothSocket> {
//    private static final String TAG = "BluetoothChat";
////    private final BluetoothServerSocket serverSocket;
//    private static final UUID MY_UUID_INSECURE =
//            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
//
//    public static final UUID MY_UUID_SECURE =
//            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
//
//
//
//    private static final String NAME_SECURE = "BluetoothChatSecure";
//    private static final String NAME_INSECURE = "BluetoothChatInsecure";
    private final BluetoothAdapter bluetoothAdapter;

    private static final Mole log = Mole.getInstance(BluetoothServerSecure.class);

    public BluetoothServerSecure(BluetoothAdapter bluetoothAdapter) {
        super(BluetoothSocket.class);
        this.bluetoothAdapter = bluetoothAdapter;
    }

//    public BluetoothServerSecure(BluetoothAdapter bluetoothAdapter){
//// Use a temporary object that is later assigned to mmServerSocket
//        // because mmServerSocket is final.
////        BluetoothServerSocket tmp = null;
////        try {
////            // MY_UUID is the app's UUID string, also used by the client code.
////            tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, MY_UUID_SECURE);
//////            bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord()
////        } catch (IOException e) {
////            Log.e(TAG, "Socket's listen() method failed", e);
////        }
////        serverSocket = tmp;
////
//
//
//
//    }

//    public BluetoothServerSecure run() {
//        BluetoothSocket socket = null;
//        // Keep listening until exception occurs or a socket is returned.
//        while (true) {
//            try {
//                socket = serverSocket.accept();
//            } catch (IOException e) {
//                Log.e(TAG, "Socket's accept() method failed", e);
//                break;
//            }
//
//            if (socket != null) {
//                // A connection was accepted. Perform work associated with
//                // the connection in a separate thread.
//                manageMyConnectedSocket(socket);
//                try {
//                    serverSocket.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                break;
//            }
//        }
//        return this;
//    }
//
//    public Handler getHandler() {
//        return handler;
//    }
//
//    public void setHandler(Handler handler) {
//        this.handler = handler;
//    }
//
//    Handler handler;
//
//    private void manageMyConnectedSocket(BluetoothSocket socket) {
//        byte[] buf = new byte[400];
//        try {
//            InputStream inputStream;
//            inputStream = socket.getInputStream();
//
//            while(0 < socket.getInputStream().read(buf)) {
//                handler.onReceive(new String(buf, "UTF-8"));
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//
//        }
//    }
//
//    // Closes the connect socket and causes the thread to finish.
//    public void cancel() {
//        try {
//            serverSocket.close();
//        } catch (IOException e) {
//            Log.e(TAG, "Could not close the connect socket", e);
//        }
//    }

    @Override
    protected BluetoothServerSocket buildConnectionProvider() {
        BluetoothServerSocket tmp = null;
        try {
            // MY_UUID is the app's UUID string, also used by the client code.

            return bluetoothAdapter.listenUsingRfcommWithServiceRecord(getName(), UUID.fromString(getUuid()));
//            bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord()
        } catch (IOException e) {
            log.e("Socket's listen() method failed", e);
            return null;
        }
    }

    @Override
    protected BluetoothSocket acceptConnection(BluetoothServerSocket bluetoothServerSocket) throws Exception {
        return bluetoothServerSocket.accept();
    }

    @Override
    protected InputStream getInputStream(BluetoothSocket bluetoothSocket) {
        try {
            return bluetoothSocket.getInputStream();
        } catch (IOException e) {
            fireError(e);
        }
        return null;
    }

    @Override
    protected OutputStream getOutputStream(BluetoothSocket bluetoothSocket) {
        try {
            return bluetoothSocket.getOutputStream();
        } catch (IOException e) {
            fireError(e);
        }
        return null;
    }

}
