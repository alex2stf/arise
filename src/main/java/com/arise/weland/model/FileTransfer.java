package com.arise.weland.model;

import com.arise.astox.net.models.AbstractStreamedSocketClient;
import com.arise.astox.net.models.ServerRequest;
import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.astox.net.models.http.HttpResponseBuilder;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.Util;
import com.arise.core.tools.models.CompleteHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class FileTransfer  {

    public static class FileRequest extends ServerRequest {
        final File file;

        public FileRequest(File file) {
            this.file = file;
        }
    }

    public static class Client extends AbstractStreamedSocketClient<FileRequest, HttpResponse> {




        @Override
        protected void write(Socket socket, FileRequest request) {

            try {
                ContentType contentType = ContentType.search(request.file);
//                String header = ("POST HTTP/1.0 /transfer?name="+request.file.getName()+"\r\n" +
//                        "Content-Type: "+ contentType +"; \r\n\r\n" );

                HttpRequest httpRequest = new HttpRequest();
                httpRequest.setUri("/transfer?name=" + request.file.getName());
                httpRequest.addHeader("Content-Type", contentType.toString());

                socket.getOutputStream().write(httpRequest.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                FileInputStream in = new FileInputStream(request.file);
                OutputStream out = socket.getOutputStream();
                byte[] bytes = new byte[16 * 1024];
                int count;
                while ((count = in.read(bytes)) > 0) {
                    out.write(bytes, 0, count);
                }

//                try {
//                    Thread.sleep(4000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }


//                HttpResponseBuilder httpResponseBuilder = new HttpResponseBuilder();
//                httpResponseBuilder.readInputStream(socket.getInputStream(), new CompleteHandler<HttpResponse>() {
//                    @Override
//                    public void onComplete(HttpResponse data) {
//                        System.out.println("server responded with" + data);
//                    }
//                });
//                Util.close(in);
                Util.close(out);
//                Util.close(socket);
//                StreamUtil.transfer(fileInputStream, socket.getOutputStream());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }



        }

        @Override
        protected void read(Socket socket, CompleteHandler<HttpResponse> responseHandler) {

            String x;
            InputStream response = null;
            do {
                try {
                    response = socket.getInputStream();
                } catch (Exception e) {
                    Util.close(response);
                    Util.close(socket);
                    responseHandler.onComplete(HttpResponse.oK());
                    return;
                }
                x = StreamUtil.toString(response);
                System.out.println("responde x = " + x);
            } while (!"copied".equals(x.trim()));

            Util.close(response);
            Util.close(socket);
            responseHandler.onComplete(HttpResponse.oK());
        }
    }

}

