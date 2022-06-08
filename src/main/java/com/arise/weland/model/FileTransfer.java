package com.arise.weland.model;

import com.arise.astox.net.models.AbstractStreamedSocketClient;
import com.arise.astox.net.models.ServerRequest;
import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.core.models.Handler;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static com.arise.astox.net.models.http.HttpResponse.oK;
import static com.arise.core.tools.StringUtil.hasText;

public class FileTransfer  {

    public static class FileRequest extends ServerRequest {
        final File f;
        final String n;

        public FileRequest(File f, String n) {
            this.f = f;
            this.n = hasText(n) ? n : f.getName();
        }
    }

    public static class Client extends AbstractStreamedSocketClient<FileRequest, HttpResponse> {


        @Override
        protected void write(Socket s, FileRequest r) {

            try {
                ContentType ct = ContentType.search(r.f);

                HttpRequest req = new HttpRequest();
                req.setUri("/transfer?name=" + r.n);
                req.addHeader("Content-Type", ct.toString());
                s.getOutputStream().write(req.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                FileInputStream in = new FileInputStream(r.f);
                OutputStream out = s.getOutputStream();
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
        protected void read(Socket _s, Handler<HttpResponse> _h) {

            String x;
            InputStream response = null;
            do {
                try {
                    response = _s.getInputStream();
                } catch (Exception e) {
                    Util.close(response);
                    Util.close(_s);
                    _h.handle(oK());
                    return;
                }
                x = StreamUtil.toString(response);
                System.out.println("responde x = " + x);
            } while (!"copied".equals(x.trim()));

            Util.close(response);
            Util.close(_s);
            _h.handle(oK());
        }
    }

}

