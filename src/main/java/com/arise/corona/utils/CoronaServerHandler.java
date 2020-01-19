package com.arise.corona.utils;


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
import com.arise.corona.dto.*;
import com.arise.corona.impl.ContentInfoDecoder;
import com.arise.corona.impl.ContentInfoProvider;


import java.io.File;
import java.util.Map;

public class CoronaServerHandler extends HTTPServerHandler {
  public static final String MSG_RECEIVE_OK = "MSG-RECEIVE-OK";
  private Mole log = Mole.getInstance(CoronaServerHandler.class);

  private static final DeviceStat deviceStat = new DeviceStat();
  static {
      deviceStat.scanIPV4();
  }

//  final Registry registry;
  MJPEGResponse liveMjpegStream;
  JPEGOfferResponse liveJpeg;
  WavResponse liveWav;

  public CoronaServerHandler setLiveMjpegStream(MJPEGResponse liveMjpegStream) {
    this.liveMjpegStream = liveMjpegStream;
    return this;
  }

  public CoronaServerHandler setLiveJpeg(JPEGOfferResponse liveJpeg) {
    this.liveJpeg = liveJpeg;
    return this;
  }

  public CoronaServerHandler setLiveWav(WavResponse liveWav) {
    this.liveWav = liveWav;
    return this;
  }



  public CoronaServerHandler() {

  }



  @Override
  public boolean validate(ServerRequest request) {
    return true;
  }

  ContentInfoProvider contentInfoProvider = new ContentInfoProvider(new ContentInfoDecoder())
          .addRoot(new File("/home"))
          .addRoot(new File("/media/alex"))
          .get();

  Handler<HttpRequest> liveMjpegHandler;
  Handler<HttpRequest> liveJpegHandler;
  Handler<HttpRequest> deviceControlsUpdate;
  Handler<HttpRequest> fileOpenHandler;
  Handler<HttpRequest> commandExecHandler;
  Handler<Message> messageHandler;



  public CoronaServerHandler onCommandExecRequest(Handler<HttpRequest> commandExecHandler) {
    this.commandExecHandler = commandExecHandler;
    return this;
  }

  public CoronaServerHandler onFileOpenRequest(Handler<HttpRequest> fileOpenHanlder) {
    this.fileOpenHandler = fileOpenHanlder;
    return this;
  }

  public CoronaServerHandler onMessageReceived(Handler<Message> messageHandler) {
    this.messageHandler = messageHandler;
    return this;
  }

  public CoronaServerHandler beforeLiveMJPEG(Handler<HttpRequest> liveMjpegRequest) {
    this.liveMjpegHandler = liveMjpegRequest;
    return this;
  }

  public CoronaServerHandler beforeLiveJPEG(Handler<HttpRequest> liveJpegHandler) {
    this.liveJpegHandler = liveJpegHandler;
    return this;
  }

  public CoronaServerHandler onDeviceControlsUpdate(Handler<HttpRequest> deviceControlsUpdate) {
    this.deviceControlsUpdate = deviceControlsUpdate;
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

    if(request.pathsStartsWith("media", "list")){
      Integer index = request.getQueryParamInt("index");
      String what = request.getPathAt(2);
      if (what == null){
        what = "pictures";
      }
      ContentPage page = contentInfoProvider.getPage(what, index);
      return HttpResponse.json(page.toString());
    }

    if (request.pathsStartsWith("media", "state", "save")){

    }

    if (request.pathsStartsWith("media", "state", "get")){

    }


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

