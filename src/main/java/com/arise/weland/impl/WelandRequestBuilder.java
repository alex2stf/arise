package com.arise.weland.impl;

import com.arise.astox.net.models.http.HttpRequestBuilder;
import com.arise.core.tools.Mole;

import java.util.UUID;

public class WelandRequestBuilder extends HttpRequestBuilder {

    private static final Mole log = Mole.getInstance(WelandRequestBuilder.class);


    String getTitleFromHeaderLine(String headerLine){
        String headerLines[] = headerLine.split("\n");

        for (String hline: headerLines){
            if(hline.indexOf(":") > -1){
                String hparts[] = hline.split(":");
                if (hparts.length > 1){
                    String value = hparts[1].trim();
                    String headerKey = hparts[0].trim();

                    if (value.indexOf(";") > -1 ){
                        String vparts[] = value.split(";");

                        if (vparts.length > 0){
                            for (String vp: vparts){
                                if (vp.indexOf("=") > -1){
                                    String kv[] = vp.trim().split("=");
                                    if (kv.length > 1) {
                                        String key = kv[0].trim();
                                        String val = kv[1].trim();
                                        if ("filename".equalsIgnoreCase(key)){
                                            return val.replaceAll("\"", "");
                                        }
//                                        if ("name".equalsIgnoreCase(key) &&  "Content-Disposition".equalsIgnoreCase(headerKey)){
//                                            return val.replaceAll("\"", "");
//                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return UUID.randomUUID().toString();
    }


    /*
    @Override
    public void readInputStream(final InputStream inputStream,
                                final Handler<HttpRequest> onSuccess,
                                final Handler<Throwable> onError) {

        HttpRequestReader reader = new HttpRequestReader() {

            //String.valueOf(bodyBytes.size()).equalsIgnoreCase(request.getHeaderParam("Content-Length"))
            @Override
            public void handleRest(HttpReader reader) {
                byte[] bytes = this.bodyBytes.toByteArray();

                if (request.pathsStartsWith("upload")){



                    String contentType = request.getHeaderParam("Content-Type");
                    String boundary = contentType.split(";")[1].trim().substring("boundary=".length());


                    try {
                        if(contentType.indexOf("multipart/form-data") > -1){

                            ByteQueue byteQueue = new ByteQueue(boundary.length() * 4);

                            boolean headerReaded = false;
                            String fileName = UUID.randomUUID().toString();

                            Map<String, FileOutputStream> streamMap = new HashMap<>();


                            for (int i = 0; i < bytes.length; i++){
                                byteQueue.add(bytes[i]);
                                if (!headerReaded && byteQueue.endsWith("\r\n\r\n")){
                                    String header = new String(byteQueue.getBytes());
                                    fileName = getTitleFromHeaderLine(header);
                                    headerReaded = true;
                                    log.info("WRITING FILE " + header);

                                } else if (headerReaded){

                                    if(byteQueue.endsWith(boundary)){
                                        streamMap.values().forEach(new Consumer<FileOutputStream>() {
                                            @Override
                                            public void accept(FileOutputStream fileOutputStream) {
                                                Util.close(fileOutputStream);
                                            }
                                        });
                                        headerReaded = false;
                                    } else {
                                        FileOutputStream fos;
                                        if(streamMap.containsKey(fileName)){
                                            fos = streamMap.get(fileName);
                                        } else {
                                            fos =  new FileOutputStream(new File(FileUtil.getUploadDir(), fileName));
                                            streamMap.put(fileName, fos);
                                        }
                                        fos.write(bytes[i]);
                                    }
                                } else {
                                    System.out.println(" CE DRACU ARE????");
                                }
                            }

                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    getRequest().setBytes(bodyBytes.toByteArray());
                    onSuccess.handle(this.getRequest());
                    flush();
                }
                else {
                    getRequest().setBytes(bodyBytes.toByteArray());
                    onSuccess.handle(this.getRequest());
                    flush();
                }
            }

            @Override
            public void onError(Throwable e) {
                onError.handle(e);
            }
        };

        reader.readInputStream(inputStream);

    }
*/


}
