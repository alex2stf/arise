package com.arise;


import com.arise.astox.net.models.AbstractPeer;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Random;

public class StressTests {
    public static final int MAX_Y = 400;
    public static final int MAX_X = 400;
    public static final int FIVE_SECONDS = 5000;

    public static void main(final String[] args) throws IOException, InterruptedException {

//        Random random = new Random();
//        java.awt.Robot r = null;
//        try {
//            r = new java.awt.Robot();
//            java.awt.GraphicsEnvironment graphicsEnvironment = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
//            java.awt.GraphicsDevice graphicsDevice = graphicsEnvironment.getScreenDevices()[0];
//            for (int i = 0; i < graphicsDevice.getDisplayMode().getHeight(); i++){
//                r.mouseMove(0, i);
//                System.out.println("i = " + i );
//                Thread.sleep(10);
//
//            }
//        } catch (AWTException e) {
//            e.printStackTrace();
//        }


//        for (int i = 0; i < Integer.valueOf(args[2]); i++){
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        tst(args[0], Integer.valueOf(args[1]), "true".equals(args[3]));
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
//        }

    }

    public static void tst(String host, Integer port, boolean sendFull) throws IOException {

        System.setProperty("http.proxyHost", "gateway.bcr-dev.qualitance.com");
        System.setProperty("http.proxyPort", port.toString());

        SSLContext sslContext = null;
        try {
            sslContext = AbstractPeer.sslContextTLSV12AllowAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Socket socket;
        socket = sslContext.getSocketFactory().createSocket(host, port);
        ((SSLSocket) socket).startHandshake();





        String postBody = "POST /api/dh/api/documents/4120/upload HTTP/1.1\r\n"
            + "Host:"+host+"\r\n"
            + "Origin:chrome-extension://fhbjgbiflinjbdggehcddcbncdddomop\r\n"
            + "Authorization:Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbl8xIiwiYXV0aCI6IlJPTEVfQURNSU4iLCJleHRUb2tlbiI6ImFkbWluXzEiLCJpbnRlcm5hbFVzZXIiOnRydWUsImV4cCI6MTU1MDU3NDM0OCwibG9naW5UeXBlIjoiQURNSU4iLCJsb2FucyI6W119.PdXs9rMPHe0peKvsa6r7q30cfknK5YuakjtfUJLn1fcvBYCeL48yY2RbWl4s1QhGkMToLTUMNfOw7y0MF6WyjA\r\n"
            + "Cache-Control:no-cache\r\n"
            + "Accept:*/*\r\n"
            + "Connection:keep-alive\r\n"
            + "User-Agent:Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/71.0.3578.98 Chrome/71.0.3578.98 Safari/537.36\r\n"

            + "Postman-Token:01e92479-9231-9495-f52f-1f2ccbd41a42\r\n"
            + "Accept-Encoding:gzip, deflate, br\r\n"
            + "Accept-Language:en-US,en;q=0.9\r\n"
            + "Content-Length:191\r\n"
            + "Content-Type:multipart/form-data; boundary=----WebKitFormBoundaryITN2oJdQ21C8zKdp\r\n"
            + "\r\n"
            + "------WebKitFormBoundaryITN2oJdQ21C8zKdp\r\n"
            + "Content-Disposition: form-data; name=\"file\"; filename=\"test.pdf\"\r\n"
            + "Content-Type: application/pdf\r\n"
            + "\r\n"
            + "xxx";
        if (sendFull){
            postBody  = postBody + "\r\n"
                + "\r\n"
                + "------WebKitFormBoundaryITN2oJdQ21C8zKdp--\r\n";
        }



            try {
//                System.out.println("start write to " + socket.getInetAddress().getHostAddress());


                socket.getOutputStream().write(postBody.getBytes());

//                System.out.println("end write, start read");


                BufferedReader in =
                    new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                String input;
                while ((input = in.readLine() )!= null){
                    System.out.println(input);
                }

                System.out.println("DONE");
                socket.close();
                Thread.currentThread().interrupt();

            } catch (IOException e) {
                e.printStackTrace();
            }

    }
}
