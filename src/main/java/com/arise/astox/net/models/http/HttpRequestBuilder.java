package com.arise.astox.net.models.http;

import com.arise.astox.net.models.ServerRequestBuilder;
import com.arise.core.models.Handler;
import com.arise.core.tools.CollectionUtil;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.Util;

import java.io.*;


public class HttpRequestBuilder extends ServerRequestBuilder<HttpRequest> {
    @Override
    public void readInputStream(InputStream in, Handler<HttpRequest> onComplete, Handler<Throwable> onError) {

        HttpRequest request = new HttpRequest();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {



            int sumRead = 0;
            byte[] buffer = new byte[9999];
            int actualRead  = in.read(buffer);
            sumRead = sumRead + actualRead;
            readChunk(buffer, baos, request);


           //aici intra numai daca are si body cu content-length definit
            if(request.getContentLength() > 0){
                while (sumRead < request.getContentLength() + request.getHeaderLength() ){
                    actualRead  = in.read(buffer);

                    readChunk(buffer, baos, request);
                    System.out.println("readed = " + actualRead);
                    sumRead += actualRead;
//                baos.write(buffer, 0, readed);
                }

            }


           onComplete.handle(request);

        } catch (Exception e) {
           onError.handle(e);
        }
    }

    private boolean readChunk(byte[] buffer, ByteArrayOutputStream bodyBytes, HttpRequest request) throws Exception {



        for (int i = 0; i < buffer.length; i++){
            bodyBytes.write(buffer[i]);

            if (buffer[i] == '\n' && !request.isHeaderReadComplete()){
                String line = bodyBytes.toString("UTF-8").trim();
                if(line.equalsIgnoreCase("")){
                    request.setHeaderReadComplete(true);
                    bodyBytes.reset();
                } else {
                    request.putLine(line);
                    bodyBytes.reset();
                }
            }

            else if (buffer[i] == '\n' && request.isMultipartFormData()){
                String line = bodyBytes.toString("UTF-8").trim();
                System.out.println("multipart line: " + line);
                bodyBytes.reset();
            }

        }




        return false;
    }


//    @Override
//    public void readInputStream(final InputStream inputStream,
//                                final Handler<HttpRequest> onSuccess,
//                                final Handler<Throwable> onError) {
//        HttpRequestReader reader = new HttpRequestReader() {
//            @Override
//            public void handleRest(HttpReader x) {
//                    getRequest().setBytes(bodyBytes.toByteArray());
//                    onSuccess.handle(this.getRequest());
//                    flush();
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                onError.handle(e);
//            }
//        };
//        reader.readInputStream(inputStream);
//    }





}
