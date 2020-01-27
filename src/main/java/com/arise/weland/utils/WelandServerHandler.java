package com.arise.weland.utils;



import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.models.ServerRequest;
import com.arise.astox.net.models.ServerResponse;
import com.arise.astox.net.serviceHelpers.HTTPServerHandler;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.MapObj;
import com.arise.core.tools.Mole;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.models.CompleteHandler;
import com.arise.weland.dto.*;
import com.arise.weland.impl.ContentInfoProvider;


import java.util.Map;

public class WelandServerHandler extends HTTPServerHandler {
  public static final String MSG_RECEIVE_OK = "MSG-RECEIVE-OK";
  private Mole log = Mole.getInstance(WelandServerHandler.class);

  private static final DeviceStat deviceStat = new DeviceStat();
  static {
      deviceStat.scanIPV4();
  }

//  final Registry registry;
  MJPEGResponse liveMjpegStream;
  JPEGOfferResponse liveJpeg;
  WavResponse liveWav;

  public WelandServerHandler setLiveMjpegStream(MJPEGResponse liveMjpegStream) {
    this.liveMjpegStream = liveMjpegStream;
    return this;
  }

  public WelandServerHandler setLiveJpeg(JPEGOfferResponse liveJpeg) {
    this.liveJpeg = liveJpeg;
    return this;
  }

  public WelandServerHandler setLiveWav(WavResponse liveWav) {
    this.liveWav = liveWav;
    return this;
  }



  public WelandServerHandler() {

  }



  @Override
  public boolean validate(ServerRequest request) {
    return true;
  }


  public WelandServerHandler setContentProvider(ContentInfoProvider contentProvider){
    this.contentInfoProvider = contentProvider;
    return this;
  }

  ContentInfoProvider contentInfoProvider;
  /**
   *  = new ContentInfoProvider(new ContentInfoDecoder())
   *           .addRoot(new File("/home"))
   *           .addRoot(new File("/media/alex"))
   *           .get();
   */

  Handler<HttpRequest> liveMjpegHandler;
  Handler<HttpRequest> liveJpegHandler;
  Handler<HttpRequest> deviceControlsUpdate;
  Handler<HttpRequest> fileOpenHandler;
  Handler<HttpRequest> commandExecHandler;
  Handler<Message> messageHandler;



  public WelandServerHandler onCommandExecRequest(Handler<HttpRequest> commandExecHandler) {
    this.commandExecHandler = commandExecHandler;
    return this;
  }

  public WelandServerHandler onFileOpenRequest(Handler<HttpRequest> fileOpenHanlder) {
    this.fileOpenHandler = fileOpenHanlder;
    return this;
  }

  public WelandServerHandler onMessageReceived(Handler<Message> messageHandler) {
    this.messageHandler = messageHandler;
    return this;
  }

  public WelandServerHandler beforeLiveMJPEG(Handler<HttpRequest> liveMjpegRequest) {
    this.liveMjpegHandler = liveMjpegRequest;
    return this;
  }

  public WelandServerHandler beforeLiveJPEG(Handler<HttpRequest> liveJpegHandler) {
    this.liveJpegHandler = liveJpegHandler;
    return this;
  }

  public WelandServerHandler onDeviceControlsUpdate(Handler<HttpRequest> deviceControlsUpdate) {
    this.deviceControlsUpdate = deviceControlsUpdate;
    return this;
  }


  public WelandServerHandler onPlayAdvice(CompleteHandler<ContentInfo> contentInfoCompleteHandler){
      this.contentInfoProvider.onPlayAdvice(contentInfoCompleteHandler);
      return this;
  }

  public <I> HttpResponse dispatch(Handler<I> handler, I data){
    if (handler != null){
      return handler.handle(data);
    }
    return HttpResponse.oK();
  }




  @Override
  public HttpResponse getHTTPResponse(HttpRequest request, AbstractServer server) {


    deviceStat.setServerUUID(server.getUuid());
    deviceStat.setDisplayName(SYSUtils.getDeviceName());


    if ("/message".equalsIgnoreCase(request.path()) && !"GET".equalsIgnoreCase(request.method())){
      deviceStat.setServerStatus(MSG_RECEIVE_OK);
      MapObj mapObj = (MapObj) Groot.decodeBytes(request.payload());
      Message message = Message.fromMap(mapObj);
      dispatch(messageHandler, message);
      return HttpResponse.json(deviceStat.toJson());
    }

    if ("/device/controls/set".equalsIgnoreCase(request.path())){
      dispatch(deviceControlsUpdate, request);
      return HttpResponse.json(deviceStat.toJson());
    }

    if ("/device/live/audio.wav".equalsIgnoreCase(request.path())){
      return liveWav;
    }

    if ("/device/live/mjpeg".equalsIgnoreCase(request.path())){
      dispatch(liveMjpegHandler, request);
      return liveMjpegStream;
    }

    if ("/device/live/jpeg".equalsIgnoreCase(request.path())){
      dispatch(liveJpegHandler, request);
      return liveJpeg;
    }

    if ("/health".equalsIgnoreCase(request.path())){
      return HttpResponse.json(deviceStat.toJson());
    }

    if ("/device/stat".equals(request.path())){
      return HttpResponse.json(deviceStat.toJson());
    }

    if (request.pathsStartsWith("thumbnail")){
      String id = request.getPathAt(1);
      return contentInfoProvider.getMediaPreview(id);
    }

    //list media based on type
    if(request.pathsStartsWith("media", "list")){
      Integer index = request.getQueryParamInt("index");
      String what = request.getPathAt(2);
      if (what == null){
        what = "pictures";
      }
      ContentPage page = contentInfoProvider.getPage(what, index);
      return HttpResponse.json(page.toString());
    }


    if ("/queue/add".equalsIgnoreCase(request.path()) && !"GET".equalsIgnoreCase(request.method())){
      Map obj = (Map) Groot.decodeBytes(request.payload());
      ContentInfo info = contentInfoProvider.getDecoder().find(obj);
      contentInfoProvider.addToQueue(info);
      return HttpResponse.oK();
    }

    if (request.pathsStartsWith("media", "shuffle")){
      String playlistId = request.getPathAt(2);
      contentInfoProvider.shuffle(playlistId);
      return HttpResponse.oK();
    }

    if (request.pathsStartsWith("media", "autoplay")){
      String playlistId = request.getPathAt(2);
      String mode = request.getPathAt(3);
      AutoplayMode autoplayMode = null;
      try {
        autoplayMode = AutoplayMode.valueOf(mode);
      }catch (Throwable t){
        return HttpResponse.plainText(t.getMessage());
      }
      contentInfoProvider.setAutoplay(playlistId, autoplayMode);
      return HttpResponse.oK();
    }

//    if (request.pathsStartsWith("media", "next")){
//      return HttpResponse.json(contentInfoProvider.next("music").toString());
//    }
    








    if (request.pathsStartsWith("files", "open")){
      dispatch(fileOpenHandler, request);
      return HttpResponse.plainText(request.getQueryParam("path"));
    }

    if (request.pathsStartsWith("commands", "exec")){
      return dispatch(commandExecHandler, request);
    }


    if ("/ping".equals(request.path())){
      HttpResponse serverResponse = HttpResponse.oK();
      for (Map.Entry<String, String> entry : request.getHeaders().entrySet()){
        serverResponse.addHeader(entry.getKey() + "-Response" , entry.getValue() + "-Response");
      }
      serverResponse.setText("PONG");
      return serverResponse;
    }

    return super.getHTTPResponse(request, server);
  }





  public interface Handler<T> {
    HttpResponse handle(T data);
  }


  @Override
  public ServerResponse getDefaultResponse(AbstractServer server) {
    return HttpResponse.plainText("bad request");
  }




}

