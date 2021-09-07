package com.arise.weland.model;

import com.arise.astox.net.models.AbstractStreamedSocketClient;
import com.arise.astox.net.models.ServerRequest;
import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.astox.net.models.http.HttpResponseBuilder;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;
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
        final String fileName;

        public FileRequest(File file, String fileName) {
            this.file = file;
            this.fileName = StringUtil.hasText(fileName) ? fileName : file.getName();
        }
    }

    public static class Client extends AbstractStreamedSocketClient<FileRequest, HttpResponse> {




        @Override
        protected void write(Socket socket, FileRequest request) {

            try {
                ContentType contentType = ContentType.search(request.file);

                HttpRequest httpRequest = new HttpRequest();
                httpRequest.setUri("/transfer?name=" + request.fileName);
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
                Util.close(out);
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

