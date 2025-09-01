package com.arise.astox.net.models.http;

import com.arise.astox.net.models.ServerRequestBuilder;
import com.arise.core.models.Handler;
import com.arise.core.tools.CollectionUtil;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.Util;

import java.io.*;
import java.util.UUID;


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
                    sumRead += actualRead;
                }

            }


           System.out.println("POST_ READ");
            request.closeMFilesResidue();
           onComplete.handle(request);

        } catch (Exception e) {
           onError.handle(e);
        }
    }





    private boolean readChunk(byte[] buffer, ByteArrayOutputStream bodyBytes, HttpRequest request) throws Exception {
        for (int i = 0; i < buffer.length; i++){
            if (!request.isHeaderReadComplete() ){
                if(buffer[i] == '\n') {
                    String line = bodyBytes.toString("UTF-8").trim();
                    if(line.equalsIgnoreCase("")){
                        request.setHeaderReadComplete(true);
                        bodyBytes.reset();
                    } else {
                        request.putHeaderLine(line);
                        bodyBytes.reset();
                    }
                }
                else {
                   bodyBytes.write(buffer[i]);
                }
            }
            else if (request.isMultipartFormData()){
                if (!request.mFileCanWrite()){
                    if(buffer[i] == '\n'){
                        String line = bodyBytes.toString("UTF-8").trim();
                        if (request.mFileIsNull() && line.endsWith(request.getBoundary())){
                            request.startMFile();
                            bodyBytes.reset();
                        }
                        else if (!request.mFileIsNull() && !request.mFileCanWrite()){
                            if (line.trim().equalsIgnoreCase("") && request.mFileHasHeaders()){
                                request.setCanWriteMfile(true);
                            } else {
                                request.mFileAddHeaderLine(line);
                            }
                            bodyBytes.reset();
                        }
                    } else {
                        bodyBytes.write(buffer[i]);
                    }
                }
                else {
                    request.mFileWrite(buffer[i], bodyBytes);
                }

            }
            else {
                bodyBytes.write(buffer[i]);
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
