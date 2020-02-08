package com.arise.weland.utils;



import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.models.ServerRequest;
import com.arise.astox.net.models.ServerResponse;
import com.arise.astox.net.serviceHelpers.HTTPServerHandler;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.serializers.parser.Whisker;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.MapObj;
import com.arise.core.tools.Mole;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.models.CompleteHandler;
import com.arise.weland.dto.*;
import com.arise.weland.impl.ContentInfoProvider;
import com.arise.weland.model.ContentHandler;


import java.util.HashMap;
import java.util.Map;

public class WelandServerHandler extends HTTPServerHandler {
  public static final String MSG_RECEIVE_OK = "MSG-RECEIVE-OK";
  private Mole log = Mole.getInstance(WelandServerHandler.class);

  public static final DeviceStat deviceStat = new DeviceStat();
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
    if (contentHandler != null){
      contentHandler.setContentInfoProvider(contentInfoProvider);
    }
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
  Handler<HttpRequest> liveWavHandler;
  Handler<HttpRequest> deviceControlsUpdate;
  Handler<HttpRequest> commandExecHandler;
  Handler<Message> messageHandler;
  ContentHandler contentHandler;

  public WelandServerHandler setContentHandler(ContentHandler contentHandler) {
    this.contentHandler = contentHandler;
    if (contentInfoProvider != null) {
      contentHandler.setContentInfoProvider(contentInfoProvider);
    }
    return this;
  }

  public WelandServerHandler onCommandExecRequest(Handler<HttpRequest> commandExecHandler) {
    this.commandExecHandler = commandExecHandler;
    return this;
  }



  public WelandServerHandler onMessageReceived(Handler<Message> messageHandler) {
    this.messageHandler = messageHandler;
    return this;
  }

  public WelandServerHandler beforeLiveWAV(Handler<HttpRequest> liveWavHandler) {
    this.liveWavHandler = liveWavHandler;
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




  public <I> HttpResponse dispatch(Handler<I> handler, I data){
    if (handler != null){
      return handler.handle(data);
    }
    return HttpResponse.oK();
  }


  Whisker whisker = new Whisker();
  String playerContent = StreamUtil.toString(FileUtil.findStream("weland/player.html"));

  @Override
  public HttpResponse getHTTPResponse(HttpRequest request, AbstractServer server) {

    String correlationId = "";
    if (StringUtil.hasText(request.getHeaderParam("Correlation-Id"))){
      correlationId = request.getHeaderParam("Correlation-Id");
    }

    deviceStat.setServerUUID(server.getUuid());
    deviceStat.setDisplayName(SYSUtils.getDeviceName());


    if ("/message".equalsIgnoreCase(request.path()) && !"GET".equalsIgnoreCase(request.method())){
      deviceStat.setServerStatus(MSG_RECEIVE_OK);
      MapObj mapObj = (MapObj) Groot.decodeBytes(request.payload());
      Message message = Message.fromMap(mapObj);
      dispatch(messageHandler, message);
      return HttpResponse.json(deviceStat.toJson()).addCorelationId(correlationId);
    }

    if ("/device/controls/set".equalsIgnoreCase(request.path())){
      dispatch(deviceControlsUpdate, request);
      return HttpResponse.json(deviceStat.toJson()).addCorelationId(correlationId);
    }

    if ("/device/live/audio.wav".equalsIgnoreCase(request.path())){
      dispatch(liveWavHandler, request);
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

    //used by android app for audio streaming services
    if ("/player".equalsIgnoreCase(request.path())){
      Map<String, String> args = new HashMap<>();
      args.put("imgSrc", request.getQueryParam("imgSrc"));
      args.put("audioSrc", request.getQueryParam("audioSrc"));
      return HttpResponse.html(whisker.compile(playerContent, args));
    }



    if ("/webcam".equalsIgnoreCase(request.path())){
      Map<String, String> args = new HashMap<>();
      args.put("imgSrc", "/device/live/mjpeg");
      args.put("audioSrc", "/device/live/audio.wav");
      return HttpResponse.html(whisker.compile(playerContent, args));
    }

    if ("/health".equalsIgnoreCase(request.path())){
      return HttpResponse.json(deviceStat.toJson()).addCorelationId(correlationId);
    }

    if ("/device/stat".equals(request.path())){
      return HttpResponse.json(deviceStat.toJson()).addCorelationId(correlationId);
    }

    if (request.pathsStartsWith("thumbnail")){
      String id = request.getPathAt(1);
      return contentInfoProvider.getMediaPreview(id).addCorelationId(correlationId);
    }

    //list media based on type
    if(request.pathsStartsWith("media", "list")){
      Integer index = request.getQueryParamInt("index");
      String what = request.getPathAt(2);
      Playlist playlist = Playlist.find(what);
      ContentPage page = contentInfoProvider.getPage(playlist, index);
      return HttpResponse.json(page.toString()).addCorelationId(correlationId);
    }


    if ("/queue/add".equalsIgnoreCase(request.path()) && !"GET".equalsIgnoreCase(request.method())){
      Map obj = (Map) Groot.decodeBytes(request.payload());
      ContentInfo info = contentInfoProvider.getDecoder().find(obj);
      contentInfoProvider.addToQueue(info);
      return HttpResponse.oK().addCorelationId(correlationId);
    }

    if (request.pathsStartsWith("media", "shuffle")){
      String playlistId = request.getPathAt(2);
      contentInfoProvider.shuffle(playlistId);
      return HttpResponse.oK().addCorelationId(correlationId);
    }

    if (request.pathsStartsWith("media", "autoplay")){
      String playlistId = request.getPathAt(2);
      String mode = request.getPathAt(3);
      AutoplayMode autoplayMode = null;
      try {
        autoplayMode = AutoplayMode.valueOf(mode);
      }catch (Throwable t){
        return HttpResponse.plainText(t.getMessage()).addCorelationId(correlationId);
      }
      Playlist playlist = Playlist.find(playlistId);
      contentInfoProvider.setAutoplay(playlist, autoplayMode);
      return HttpResponse.oK().addCorelationId(correlationId);
    }










    if (request.pathsStartsWith("files", "open")){
      HttpResponse response = contentHandler.play(request);
      if (response != null){
        return response;
      }
      return HttpResponse.plainText(request.getQueryParam("path")).addCorelationId(correlationId);
    }

    if (request.pathsStartsWith("files", "close")){
      HttpResponse response =  contentHandler.stop(request);
      if (response != null){
        return response;
      }
      return HttpResponse.plainText(request.getQueryParam("path")).addCorelationId(correlationId);
    }

    if (request.pathsStartsWith("files", "pause")){
      HttpResponse response =  contentHandler.pause(request);
      if (response != null){
        return response;
      }
      return HttpResponse.plainText(request.getQueryParam("path")).addCorelationId(correlationId);
    }

    if (request.pathsStartsWith("commands", "exec")){
      return dispatch(commandExecHandler, request).addCorelationId(correlationId);
    }


    if ("/ping".equals(request.path())){
      HttpResponse serverResponse = HttpResponse.oK();
      for (Map.Entry<String, String> entry : request.getHeaders().entrySet()){
        serverResponse.addHeader(entry.getKey() + "-Response" , entry.getValue() + "-Response");
      }
      serverResponse.setText("PONG").addCorelationId(correlationId);
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

