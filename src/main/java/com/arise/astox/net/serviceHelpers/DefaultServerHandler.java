package com.arise.astox.net.serviceHelpers;


import com.arise.astox.net.http.HttpRequest;
import com.arise.astox.net.http.HttpResponse;
import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.models.ServerRequest;
import com.arise.astox.net.models.ServerResponse;
import com.arise.astox.quixot.Quixot;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.ReflectUtil;
import com.arise.core.tools.StreamUtil;


import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Map;

public class DefaultServerHandler extends HTTPServerHandler {
  private Mole log = Mole.getInstance(DefaultServerHandler.class);

  static MJPEGResponse mjpegResponse = null;
  static JPEGOfferResponse jpegOfferResponse = null;

  private static final HttpResponse quixotPage = HttpResponse.javascript(Quixot.devMode(null));



  @Override
  public boolean validate(ServerRequest request) {
    return true;
  }

  @Override
  public void postInit(AbstractServer server) {
    super.postInit(server);
  }




  @Override
  public HttpResponse getHTTPResponse(HttpRequest request, AbstractServer server) {
    if ("/quixot".equals(request.path())){
      return quixotPage;
    }



    if ("/ping".equals(request.path())){
      HttpResponse serverResponse = HttpResponse.oK();

      for (Map.Entry<String, String> entry : request.getHeaders().entrySet()){
        serverResponse.addHeader(entry.getKey() + "-Response" , entry.getValue() + "-Response");
      }
      return serverResponse;
    }

    if("/jpeg-stream-test".equals(request.path()) && !ReflectUtil.classExists("android.os.Environment")){
      if (jpegOfferResponse == null){
        jpegOfferResponse = new JPEGOfferResponse();
      }
      File[] jpegs = new File("/home/alex/Pictures").listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return name.endsWith("jpg") || name.endsWith("JPG");
        }
      });
      int rand = (int) (Math.round(Math.random() * jpegs.length));
      if (rand == jpegs.length){
        rand -= 1;
      }
      try {
        jpegOfferResponse.offerJPEG(StreamUtil.read(jpegs[rand]));
      } catch (IOException e) {
        e.printStackTrace();
      }
      return jpegOfferResponse;
    }

    if("/device-cam-mjpeg-stream".equals(request.path()) && !ReflectUtil.classExists("android.os.Environment")){
      if (mjpegResponse == null){
        mjpegResponse = new MJPEGResponse() {
          @Override
          public void onTransporterAccepted(ServerRequest serverRequest, Object... args) {
            super.onTransporterAccepted(serverRequest, args);
            iterateImages(new OnFound() {
              @Override
              public void found(byte[] bytes) {
                pushJPEGFrame(bytes);
              }
            });
          }
        };
      }
      return mjpegResponse;
    }



    return super.getHTTPResponse(request, server);

  }




  public static void iterateImages(OnFound onFound){
    byte[] bytes;
    File[] jpegs = new File("/home/alex/Pictures").listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith("jpg") || name.endsWith("JPG");
      }
    });
    int i = 0;
    while (true){
      if (i > jpegs.length -1){
        i = 0;
      }
      File f = jpegs[i];

      try {

        bytes = StreamUtil.read(f);
        onFound.found(bytes);
      } catch (IOException e) {
        e.printStackTrace();
      }

      i++;
      try {
        Thread.sleep(1000 * 2);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }


  @Override
  public ServerResponse getDefaultResponse(AbstractServer server) {
    return HttpResponse.plainText("bad request");
  }


  private interface OnFound {
    void found(byte[] bytes);
  }

}

