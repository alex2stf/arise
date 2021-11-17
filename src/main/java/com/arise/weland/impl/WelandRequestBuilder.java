package com.arise.weland.impl;

import com.arise.astox.net.models.http.HttpReader;
import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpRequestBuilder;
import com.arise.astox.net.models.http.HttpRequestReader;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.Util;
import com.arise.core.tools.models.CompleteHandler;

import java.io.*;

public class WelandRequestBuilder extends HttpRequestBuilder {
    private final IDeviceController deviceController;

    private static final Mole log = Mole.getInstance(WelandRequestBuilder.class);

    public WelandRequestBuilder(IDeviceController deviceController) {
        this.deviceController = deviceController;
    }


    @Override
    public void readInputStream(final InputStream inputStream,
                                final CompleteHandler<HttpRequest> onSuccess,
                                final CompleteHandler<Throwable> onError) {

        HttpRequestReader reader = new HttpRequestReader() {

            @Override
            public void handleRest(HttpReader reader) {
                byte[] bytes = this.bodyBytes.toByteArray();

                //logica de device controller
                if (bytes.length > 0 && bytes[0] == '>'){
//                    System.out.println("RECEIVED " + new String(bytes));
                    deviceController.digestBytes(bytes);
                    resetBodyBytes();
                    this.readInputStream(inputStream);
                }

                else if (request.pathsStartsWith("transfer")){
                    String name = request.getQueryParam("name");
                    log.info("transfer file " + name);
                    try {

                        if (name.startsWith("sync-media")){
                            File f[] = FileUtil.getUploadDir().listFiles(new FilenameFilter() {
                                @Override
                                public boolean accept(File dir, String name) {
                                    return name.startsWith("sync-media");
                                }
                            });
                            if (f != null && f.length > 0){
                                for (File s: f){
                                    s.delete();
                                }
                            }
                        }

                        FileOutputStream out = new FileOutputStream(new File(FileUtil.getUploadDir(), name));

                        out.write(bytes);
                        resetBodyBytes();

                        byte[] buff = new byte[16*1024];
                        int count;
                        while ((count = inputStream.read(buff)) > 0) {
                            out.write(buff, 0, count);
                        }
                        Util.close(out);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    getRequest().setBytes(bodyBytes.toByteArray());
                    onSuccess.onComplete(this.getRequest());
                    flush();
                }
                else {
                    getRequest().setBytes(bodyBytes.toByteArray());
                    onSuccess.onComplete(this.getRequest());
                    flush();
                }
            }

            @Override
            public void onError(Throwable e) {
                onError.onComplete(e);
            }
        };

        reader.readInputStream(inputStream);

    }



}
