package com.arise.weland.utils;

import com.arise.astox.net.models.ServerRequest;
import com.arise.astox.net.models.SingletonHttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


@Deprecated
public class MJPEGResponse extends SingletonHttpResponse {

    protected static String boundary = "some-rand-boundary";


    public MJPEGResponse(){

    }


    @Override
    public void onTransporterAccepted(ServerRequest serverRequest, Object... args) {
        super.onTransporterAccepted(serverRequest, args);
        sendHeader(
                ("HTTP/1.0 200 OK\r\n" +
                        "Server: iRecon\r\n" +
                        "Connection: close\r\n" +
                        "Max-Age: 0\r\n" +
                        "Expires: 0\r\n" +
                        "Cache-Control: no-store, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0\r\n" +
                        "Pragma: no-cache\r\n" +
                        "Content-Type: multipart/x-mixed-replace; " +
                        "boundary=" + boundary + "\r\n" +
                        "\r\n" +
                        "--" + boundary + "\r\n").getBytes()
        );
    }


    protected static byte[] bytesTOMJPEG(byte[] bytes){
        if (bytes == null){
            return null;
        }
        ByteArrayOutputStream b = new ByteArrayOutputStream();

        try {
            b.write((
                    "Content-type: image/jpg\r\n" +
                            "Content-Length: " + bytes.length + "\r\n" +
                            "X-Timestamp:" + System.currentTimeMillis() + "\r\n" +
                            "\r\n").getBytes());

            b.write(bytes);

            b.write(("\r\n--" + boundary + "\r\n").getBytes());
            return b.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void pushJPEGFrame(byte[] bytes){
        sendSync(bytesTOMJPEG(bytes));
    }

    @Override
    public boolean isSelfManageable() {
        return true;
    }






}
