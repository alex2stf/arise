package com.arise.weland.utils;


import com.arise.astox.net.clients.HttpClient;
import com.arise.astox.net.models.AbstractClient;
import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.models.DuplexDraft;
import com.arise.astox.net.models.ServerRequest;
import com.arise.astox.net.models.ServerResponse;
import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.astox.net.models.http.Multipart;
import com.arise.astox.net.serviceHelpers.HTTPServerHandler;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.serializers.parser.Whisker;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.MapObj;
import com.arise.core.tools.Mole;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.models.CompleteHandler;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.dto.ContentPage;
import com.arise.weland.dto.DeviceStat;
import com.arise.weland.dto.Message;
import com.arise.weland.dto.Playlist;
import com.arise.weland.impl.ContentInfoProvider;
import com.arise.weland.impl.IDeviceController;
import com.arise.weland.model.ContentHandler;
import com.arise.weland.model.FileTransfer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WelandServerHandler extends HTTPServerHandler {
  public static final String MSG_RECEIVE_OK = "MSG-RECEIVE-OK";
  private Mole log = Mole.getInstance(WelandServerHandler.class);

  public static final DeviceStat deviceStat = DeviceStat.getInstance();


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


  Handler<HttpRequest> liveMjpegHandler;
  Handler<HttpRequest> liveJpegHandler;
  Handler<HttpRequest> liveWavHandler;
  Handler<HttpRequest> deviceControlsUpdate;
  ContentHandler contentHandler;

  IDeviceController iDeviceController;

  public WelandServerHandler setDeviceController(IDeviceController iDeviceController) {
    this.iDeviceController = iDeviceController;
    return this;
  }

  public WelandServerHandler setContentHandler(ContentHandler contentHandler) {
    this.contentHandler = contentHandler;
    if (contentInfoProvider != null) {
      contentHandler.setContentInfoProvider(contentInfoProvider);
    }
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
  String appContent = StreamUtil.toString(FileUtil.findStream("weland/app.html"));


  @Override
  public HttpResponse getHTTPResponse(HttpRequest request, AbstractServer server) {

    String correlationId = "";
    if (StringUtil.hasText(request.getHeaderParam("Correlation-Id"))){
        correlationId = request.getHeaderParam("Correlation-Id");
    }

    deviceStat.setServerUUID(server.getUuid());
    deviceStat.setDisplayName(SYSUtils.getDeviceName().toUpperCase());

    if ("/message".equalsIgnoreCase(request.path()) && !"GET".equalsIgnoreCase(request.method())){
      deviceStat.setServerStatus(MSG_RECEIVE_OK);
      MapObj mapObj = (MapObj) Groot.decodeBytes(request.payload());
      Message message = Message.fromMap(mapObj);
      contentHandler.onMessageReceived(message);
      return HttpResponse.json(deviceStat.toJson()).allowAnyOrigin();
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
    if ("/app".equalsIgnoreCase(request.path())){
      appContent = StreamUtil.toString(FileUtil.findStream("src/main/resources#weland/app.html"));
      Map<String, String> args = new HashMap<>();
      args.put("host", request.getQueryParamString("host", ""));
      whisker.setTemplatesRoot("src/main/resources#weland");
      return HttpResponse.html(whisker.compile(appContent, args));
    }


    //used by android app for audio streaming services
    if ("/frame".equalsIgnoreCase(request.path())){
      Map<String, String> args = new HashMap<>();
      args.put("src", request.getQueryParam("src"));
      String frameContent = StreamUtil.toString(FileUtil.findStream("src/main/resources#weland/frame.html"));
      return HttpResponse.html(whisker.compile(frameContent, args));
    }

    if ("/health".equalsIgnoreCase(request.path())){
      return HttpResponse.json(deviceStat.toJson()).addCorelationId(correlationId).allowAnyOrigin();
    }

    if ("/device/stat".equals(request.path())){
      return HttpResponse.json(deviceStat.toJson()).addCorelationId(correlationId).allowAnyOrigin();
    }

    if (request.pathsStartsWith("thumbnail")){
      String id = request.getQueryParam("id");
      return contentInfoProvider.getMediaPreview(id).addCorelationId(correlationId).allowAnyOrigin();
    }

    //list media based on type
    if(request.pathsStartsWith("media", "list")){
      Integer index = request.getQueryParamInt("index");
      String what = request.getPathAt(2);
      Playlist playlist = Playlist.find(what);
      ContentPage page = contentInfoProvider.getPage(playlist, index);
      return HttpResponse.json(page.toString()).addCorelationId(correlationId).allowAnyOrigin();
    }


    if (request.pathsStartsWith("queue", "add") && !"GET".equalsIgnoreCase(request.method())){
      Map obj = (Map) Groot.decodeBytes(request.payload());
      ContentInfo info = contentInfoProvider.getDecoder().find(obj);
      contentInfoProvider.addToQueue(info);
      return HttpResponse.oK().addCorelationId(correlationId);
    }

    if (request.pathsStartsWith("media", "shuffle")){
      String playlistId = request.getPathAt(2);
      contentInfoProvider.shuffle(playlistId);
      return HttpResponse.oK().addCorelationId(correlationId).allowAnyOrigin();
    }

    if (request.pathsStartsWith("media", "autoplay")){
      String groupName = request.getQueryParam("group");
      String playlistId = request.getQueryParam("playlist");
      if (groupName != null){
        AutoplayOptions.setAutoplayGroup(groupName);
      }
      else if(playlistId != null && Playlist.find(playlistId) != null) {
        Playlist playlist = Playlist.find(playlistId);
        AutoplayOptions.setAutoplayPlaylist(playlist);
      }
      return HttpResponse.oK().addCorelationId(correlationId);
    }


    if (request.pathsStartsWith("files", "open") || request.pathsStartsWith("files", "play")){
      HttpResponse response = contentHandler.openRequest(request);
      if (response != null){
        return response.allowAnyOrigin();
      }
      return HttpResponse.plainText(request.getQueryParam("path"))
              .addCorelationId(correlationId)
              .allowAnyOrigin();
    }


    if ("/download".equals(request.path())){
      String path = request.getQueryParam("file");
      File f = new File(path);
      ContentType contentType = ContentType.search(f);
      byte[] bytes;
      try {
        bytes = StreamUtil.fullyReadFileToBytes(f);
      } catch (IOException e) {
        return HttpResponse.plainText("ERROR");
      }

      return new HttpResponse()
              .setContentType(contentType)
              .setContentLength(bytes.length)
              .addHeader("Content-Disposition",  "attachment; filename=\"" + f.getName() + "\"")
              .setStatusCode(200)
              .setBytes(bytes)
              .allowAnyOrigin();
    }


    if(request.pathsStartsWith("info", "persisted", "playlist", "music")){
      return HttpResponse.plainText(contentInfoProvider.getPersistedPlaylistContent(Playlist.MUSIC));
    }

    if(request.pathsStartsWith("transfer")){
      //file may be already copied
      return HttpResponse.plainText("copied");
    }

    if ("/upload".equalsIgnoreCase(request.path())){
      String path = request.getQueryParam("file");
      File f = new File(path);
      if (!f.exists()){
        return HttpResponse.plainText("file does not exist");
      }

      URL url;
      try {
        url = new URL(request.getQueryParam("destination"));
      } catch (MalformedURLException e) {
        return HttpResponse.plainText("invalid destination");
      }
      FileTransfer.Client client = new FileTransfer.Client();
      client.setHost(url.getHost());
      client.setPort(url.getPort());
      client.sendAndReceive(new FileTransfer.FileRequest(f), new CompleteHandler<HttpResponse>() {
        @Override
        public void onComplete(HttpResponse data) {
//          System.out.println(data);
        }
      });
      return HttpResponse.plainText(f.getAbsolutePath());
    }




    if (request.pathsStartsWith("files", "close")){
      HttpResponse response =  contentHandler.stop(request);
      if (response != null){
        return response.allowAnyOrigin();
      }
      return HttpResponse.plainText(request.getQueryParam("path")).addCorelationId(correlationId).allowAnyOrigin();
    }



    if ("/ping".equals(request.path())){
      HttpResponse serverResponse = HttpResponse.oK();
      for (Map.Entry<String, String> entry : request.getHeaders().entrySet()){
        serverResponse.addHeader(entry.getKey() + "-Response" , entry.getValue() + "-Response");
      }
      serverResponse.setText("PONG").addCorelationId(correlationId).allowAnyOrigin();
      return serverResponse;
    }



    if ("/kontrols".equalsIgnoreCase(request.path())){
      Map<String, Object> args = new HashMap<>();
      return HttpResponse.html(whisker.compile(StreamUtil.toString(FileUtil.findStream("src/main/resources#weland/kontrols.html")), args));
    }




    if ("/WSKontrol".equalsIgnoreCase(request.path())){
      Map<String, Object> args = new HashMap<>();
      String id = request.getQueryParam("id");
      if (!StringUtil.hasText(id)){
        id = UUID.randomUUID().toString();
      }
      args.put("ipv4Addrs", StringUtil.join(DeviceStat.getInstance().getIpv4Addrs(), ",", new StringUtil.JoinIterator<String>() {
        @Override
        public String toString(String value) {
          return StringUtil.jsonVal(value);
        }
      }));
      args.put("port", server.getPort());
      args.put("id", id);
      args.put("protocol", server.isSecure() ? "wss" : "ws");
      return HttpResponse.javascript(whisker.compile(StreamUtil.toString(FileUtil.findStream("src/main/resources#weland/WSKontrol.js")), args));
    }


    if (request.pathsStartsWith("games")){
      String id = request.getPathAt(1);
      File main = contentInfoProvider.getGame(id);

      try {
        return HttpResponse.html(FileUtil.read(main));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return super.getHTTPResponse(request, server).allowAnyOrigin();
  }


  @Override
  public void onDuplexConnect(AbstractServer ioHttp, ServerRequest request, DuplexDraft.Connection connection) {
      duplexConnections.add(connection);
  }

  @Override
  public void onFrame(DuplexDraft.Frame frame, DuplexDraft.Connection connection) {
    broadcast(frame.getPayloadText());
  }

  @Override
  public void onDuplexClose(DuplexDraft.Connection c) {
    duplexConnections.remove(c);
  }

  public interface Handler<T> {
    HttpResponse handle(T data);
  }


  @Override
  public ServerResponse getDefaultResponse(AbstractServer server) {
    return HttpResponse.plainText("bad request");
  }


}

