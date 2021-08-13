package com.arise.weland.utils;


import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.models.DuplexDraft;
import com.arise.astox.net.models.ServerRequest;
import com.arise.astox.net.models.ServerResponse;
import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.astox.net.serviceHelpers.HTTPServerHandler;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.serializers.parser.Whisker;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.MapObj;
import com.arise.core.tools.MapUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.models.CompleteHandler;
import com.arise.weland.PlaylistWorker;
import com.arise.weland.dto.ContentPage;
import com.arise.weland.dto.DeviceStat;
import com.arise.weland.dto.Message;
import com.arise.weland.dto.Playlist;
import com.arise.weland.dto.Detail;
import com.arise.weland.impl.ContentInfoProvider;
import com.arise.weland.impl.IDeviceController;
import com.arise.weland.model.ContentHandler;
import com.arise.weland.model.FileTransfer;

import java.io.File;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.arise.core.tools.StringUtil.jsonVal;

public class WelandServerHandler extends HTTPServerHandler {
  public static final String MSG_RECEIVE_OK = "MSG-RECEIVE-OK";
  public static final DeviceStat deviceStat = DeviceStat.getInstance();
  ContentInfoProvider contentInfoProvider;
  Handler<HttpRequest> deviceControlsUpdate;
  ContentHandler contentHandler;
  IDeviceController iDeviceController;
  Whisker whisker = new Whisker();
  String appContent = StreamUtil.toString(FileUtil.findStream("weland/app.html"));
  private Mole log = Mole.getInstance(WelandServerHandler.class);
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







  public WelandServerHandler onDeviceControlsUpdate(Handler<HttpRequest> deviceControlsUpdate) {
    this.deviceControlsUpdate = deviceControlsUpdate;
    return this;
  }

  @Deprecated
  public <I> HttpResponse dispatch(Handler<I> handler, I data){
    if (handler != null){
      return handler.handle(data);
    }
    return HttpResponse.oK();
  }

  @Override
  public HttpResponse getHTTPResponse(HttpRequest request, AbstractServer server) {

    if("OPTIONS".equalsIgnoreCase(request.method())){
        return HttpResponse.oK().allowAnyOrigin();
    }


    String correlationId = "";
    if (StringUtil.hasText(request.getHeaderParam("Correlation-Id"))){
        correlationId = request.getHeaderParam("Correlation-Id");
    }

    deviceStat.setServerUUID(server.getUuid());
    deviceStat.setDisplayName(SYSUtils.getDeviceName().toUpperCase());
    deviceStat.setProp("ks", "false");

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
      contentHandler.beforeLiveWav(request);
      return contentHandler.getLiveWav();
    }

    if ("/device/live/mjpeg".equalsIgnoreCase(request.path())){
      return contentHandler.getLiveMjpegStream();
    }

    if ("/device/live/jpeg".equalsIgnoreCase(request.path())){
      return contentHandler.getLiveJpeg();
    }

    //main html rendering
    if ("/app".equalsIgnoreCase(request.path()) || "/app.html".equalsIgnoreCase(request.path())){
      appContent = StreamUtil.toString(FileUtil.findStream("src/main/resources#weland/app.html"));
      Map<String, String> args = new HashMap<>();
      args.put("host", request.getQueryParamString("host", ""));
      whisker.setTemplatesRoot("src/main/resources#weland");
      return HttpResponse.html(whisker.compile(appContent, args));
    }


    //used mainly for audio streaming services
    if ("/frame".equalsIgnoreCase(request.path())){
      Map<String, String> args = new HashMap<>();
      args.put("src", request.getQueryParam("src"));
      String frameContent = StreamUtil.toString(FileUtil.findStream("src/main/resources#weland/frame.html"));
      return HttpResponse.html(whisker.compile(frameContent, args));
    }

    //generic platform agnostic information
    if ("/device/stat".equals(request.path()) || "/health".equalsIgnoreCase(request.path())){
      return DeviceStat.getInstance().toHttp(request);
    }

    //fetch thumbnail
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

    //platform specific details
    if("/device/details".equalsIgnoreCase(request.path())){
      StringBuilder sb = new StringBuilder().append("{");
      sb.append("\"CV1\":").append(Detail.toJson(contentHandler.getCameraIdsV1()));
      sb.append(",\"FMV1\":").append(Detail.toJson(contentHandler.getFlashModesV1()));
      sb.append(",\"ECAMV1\":").append(contentHandler.getEnabledCameraV1().toString());
      sb.append("}");
      return HttpResponse.json(
              sb.toString()
      ).addCorelationId(correlationId).allowAnyOrigin();
    }



    if ("/device/update".equalsIgnoreCase(request.path())){
      Map<String, String> response = contentHandler.onDeviceUpdate(request.getQueryParams());
      return HttpResponse.json(Groot.toJson(response)).allowAnyOrigin();
    }


    //open given path
    if (request.pathsStartsWith("files", "open") || request.pathsStartsWith("files", "play")){
      System.out.println(request);
      HttpResponse response = contentHandler.openRequest(request);
      
      if (response != null){
        return response.allowAnyOrigin();
      }
      return DeviceStat.getInstance().toHttp(request);
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



    //close || stopPreviews
    if (request.pathsStartsWith("files", "close")){
      contentHandler.stop(request);
      return DeviceStat.getInstance().toHttp(request);
    }

     //PAUSE
      if (request.pathsStartsWith("media", "pause")){
          HttpResponse response =  contentHandler.pauseRequest(request);
          if (response != null){
              return response.allowAnyOrigin();
          }
          return DeviceStat.getInstance().toHttp(request);
      }

      //package-info loader
      if (request.pathsStartsWith("pack")){
        String path = request.getQueryParam("root");
        File f = new File(path);
        try {
          Map props = ContentInfoProvider.packageInfoProps(f);
          File html = new File(f.getParentFile(), MapUtil.getString(props, "main"));
          return HttpResponse.html(FileUtil.read(html));
        } catch (IOException e) {
          e.printStackTrace();
          return HttpResponse.oK();
        }

      }

    if ("/ping".equals(request.path())){
      HttpResponse serverResponse = HttpResponse.oK();
      for (Map.Entry<String, String> entry : request.getHeaders().entrySet()){
        serverResponse.addHeader(entry.getKey() + "-Response" , entry.getValue() + "-Response");
      }
      serverResponse.setText("PONG").addCorelationId(correlationId).allowAnyOrigin();
      return DeviceStat.getInstance().toHttp(request);
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
          return jsonVal(value);
        }
      }));
      args.put("port", server.getPort());
      args.put("id", id);
      args.put("protocol", server.isSecure() ? "wss" : "ws");
      return HttpResponse.javascript(whisker.compile(StreamUtil.toString(FileUtil.findStream("src/main/resources#weland/WSKontrol.js")), args));
    }


    if (request.pathsStartsWith("playlist")){

      Map<String, List<String>> data = request.getQueryParams();

      String action = request.getQueryParamString("action", "xx");
      String name = request.getQueryParamString("name", null);
      String path = request.getQueryParamString("path", null);
      switch (action){
        case "create":
          PlaylistWorker.createPlaylist(name);
          break;
        case "drop":
          PlaylistWorker.dropPlaylist(name);
          break;
        case "add":
          PlaylistWorker.add(name, path);
          break;
        case "play":
          PlaylistWorker.setRunningPlaylist(name);
          contentHandler.onPlaylistPlay(name);
          break;
        case "get":
          return HttpResponse.json(PlaylistWorker.getPlaylist(name)).allowAnyOrigin();
      }




      return HttpResponse.json(PlaylistWorker.listPlaylists()).allowAnyOrigin();
    }



    return super.getHTTPResponse(request, server).allowAnyOrigin();
  }


  @Override
  public void onDuplexConnect(AbstractServer ioHttp, ServerRequest request,  DuplexDraft.Connection connection) {
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

  @Override
  public ServerResponse getDefaultResponse(AbstractServer server) {
    return HttpResponse.plainText("bad request");
  }


  @Deprecated
  public interface Handler<T> {
    HttpResponse handle(T data);
  }


}

