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
import com.arise.core.tools.*;
import com.arise.core.tools.models.CompleteHandler;
import com.arise.weland.PlaylistWorker;
import com.arise.weland.dto.*;
import com.arise.weland.impl.ContentInfoProvider;
import com.arise.weland.impl.IDeviceController;
import com.arise.weland.model.ContentHandler;
import com.arise.weland.model.FileTransfer;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.arise.core.tools.StringUtil.jsonVal;

public class WelandServerHandler extends HTTPServerHandler {
  public static final String MSG_RECEIVE_OK = "MSG-RECEIVE-OK";

  private static final ProxyHttpResponse proxyHttpResponse = new ProxyHttpResponse();


  ContentInfoProvider contentInfoProvider;
  ContentHandler contentHandler;
  IDeviceController iDeviceController;
  Whisker whisker = new Whisker()
          .setTemplatesRoot("src/main/resources#weland");
  String appContent = StreamUtil.toString(FileUtil.findStream("weland/app.html"));
  private Mole log = Mole.getInstance(WelandServerHandler.class);

  ThreadUtil.TimerResult syncPlayTimer;


  public static File getClientPropsFile(){
    return new File(FileUtil.findDocumentsDir(), "weland-client-props");
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









  @Override
  public HttpResponse getHTTPResponse(HttpRequest request, AbstractServer server) {

    if("OPTIONS".equalsIgnoreCase(request.method())){
        return HttpResponse.oK().allowAnyOrigin();
    }


    String correlationId = "";
    if (StringUtil.hasText(request.getHeaderParam("Correlation-Id"))){
        correlationId = request.getHeaderParam("Correlation-Id");
    }

    if ("/message".equalsIgnoreCase(request.path()) && !"GET".equalsIgnoreCase(request.method())){
      Map mapObj = (Map) Groot.decodeBytes(request.payload());
      Message message = Message.fromMap(mapObj);
      contentHandler.onMessageReceived(message);
      return contentHandler.getDeviceStat().toHttp();
    }

    if("/device/info".equalsIgnoreCase(request.path())){
      return HttpResponse.json(contentHandler.getDeviceInfoJson()).allowAnyOrigin();
    }


    if ("/device/controls/set".equalsIgnoreCase(request.path())){
      return contentHandler.getDeviceStat().toHttp();
    }

    if ("/device/live/audio.wav".equalsIgnoreCase(request.path())){
      return contentHandler.getLiveWav();
    }

    if ("/device/live/mjpeg".equalsIgnoreCase(request.path())){
      return contentHandler.getLiveMjpegStream();
    }

    if ("/device/live/jpeg".equalsIgnoreCase(request.path())){
      return contentHandler.getLiveJpeg();
    }

    if("/alias-play/rockfm".equalsIgnoreCase(request.path())){
      return contentHandler.openPath("https://live.rockfm.ro:8443/rockfm.aacp");
    }

    if("/alias-play/jazz".equalsIgnoreCase(request.path())){
      return contentHandler.openPath("https://stream.srg-ssr.ch/m/rsj/mp3_128");
    }

    //main html rendering
    if ("/app".equalsIgnoreCase(request.path()) || "/app.html".equalsIgnoreCase(request.path())){
      appContent = StreamUtil.toString(FileUtil.findStream("src/main/resources#weland/app.html"));
      Map<String, String> args = new HashMap<>();
      args.put("host", request.getQueryParamString("host", ""));
      return HttpResponse.html(whisker.compile(appContent, args));
    }


    if(request.path().equalsIgnoreCase("/orchestra")){
      String orchContent = StreamUtil.toString(FileUtil.findStream("src/main/resources#weland/orchestra.html"));
      Map<String, String> args = new HashMap<>();
      args.put("host", request.getQueryParamString("host", ""));

      return HttpResponse.html(whisker.compile(orchContent, args));
    }

    if(request.path().startsWith("/proxy/exec")){
      return proxyHttpResponse;
    }


    //generic platform agnostic information
    if ("/device/stat".equals(request.path()) || "/health".equalsIgnoreCase(request.path())){
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
      simpleDateFormat.setTimeZone(TimeZone.getDefault());
      return contentHandler.getDeviceStat()
              .setProp("JT", simpleDateFormat.format(new Date()))
              .toHttp();
    }

    if ("/device/update".equals(request.path()) || "/health".equalsIgnoreCase(request.path())){
      return contentHandler.onDeviceUpdate(request.getQueryParams()).toHttp();
    }


    if (request.pathsStartsWith("device-update")){
      String what = request.getPathAt(1);
      Map<String, List<String>> params = new HashMap<>();
      String mode;

      if ("lightMode".equalsIgnoreCase(what)){
        mode = request.getPathAt(2);
        params.put("lightMode", Arrays.asList(mode));
      }
      else if ("camId".equalsIgnoreCase(what)){
        mode = request.getPathAt(2);
        params.put("camId", Arrays.asList(mode));
      }
      else if ("musicVolume".equalsIgnoreCase(what)){
        mode = request.getPathAt(2);
        params.put("musicVolume", Arrays.asList(mode));
      }
      else if ("camEnabled".equalsIgnoreCase(what)){
        mode = request.getPathAt(2);
        params.put("camEnabled", Arrays.asList(mode));
      }
      return contentHandler.onDeviceUpdate(params).toHttp();
    }



    if("/props/get".equals(request.path())){
      String key = request.getQueryParam("key");

      try {
        Properties clientProps = FileUtil.loadProps(getClientPropsFile());
        if (clientProps.containsKey(key)){
          return HttpResponse.plainText(clientProps.getProperty(key)).allowAnyOrigin();
        }
      } catch (Exception e) {
        log.error("Failed to get property" + key);
      }
      return HttpResponse.plainText("").allowAnyOrigin();
    }

    if("/props/set".equals(request.path())){
      String key = request.getQueryParam("key");
      String value = request.getQueryParam("value");

      //TODO intelege de ce asa
      try {
        value = URLDecoder.decode(value, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        ;;
      }

      try {
        File propsFile = getClientPropsFile();
        Properties clientProps;
        if (!propsFile.exists()){
          clientProps = new Properties();
        } else {
          clientProps = FileUtil.loadProps(getClientPropsFile());
        }
        clientProps.put(key, value);
        FileUtil.saveProps(clientProps, getClientPropsFile(), "");
        return HttpResponse.plainText(clientProps.getProperty(key)).allowAnyOrigin();
      } catch (Exception e) {
        log.error(e);
        return HttpResponse.plainText(e.getMessage()).allowAnyOrigin();
      }

    }

    //fetch thumbnail
    if (request.pathsStartsWith("thumbnail")){
      String id = request.getQueryParam("id");
      return contentInfoProvider.getMediaPreview(id)
              .addCorelationId(correlationId).allowAnyOrigin();
    }



    if(request.pathsStartsWith("snapshot-get")){
      return contentHandler.getLatestSnapshot().addCorelationId(correlationId).allowAnyOrigin();
    }

    if(request.pathsStartsWith("snapshot-make")){
      contentHandler.takeSnapshot();
      return contentHandler.getLatestSnapshot().addCorelationId(correlationId).allowAnyOrigin();
    }

    //list media based on type
    if(request.pathsStartsWith("media", "list")){
      Integer index = request.getQueryParamInt("index");
      String what = request.getPathAt(2);
      Playlist playlist = Playlist.find(what);
      ContentPage page = contentInfoProvider.getPage(playlist, index);
      return HttpResponse.json(page.toString()).addCorelationId(correlationId).allowAnyOrigin();
    }




    //open given path
    if (request.pathsStartsWith("files", "open") || request.pathsStartsWith("files", "play")){
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
      return HttpResponse.plainText(contentInfoProvider.getPlaylistFileContent(Playlist.MUSIC));
    }

    if(request.pathsStartsWith("transfer")){
      return HttpResponse.plainText("copied");
    }

    if ("/upload/stat".equalsIgnoreCase(request.path())){
      String fileToCheck = request.getQueryParam("name");
      File f = new File(FileUtil.getUploadDir(), fileToCheck);
      if (!f.exists()){
        return HttpResponse.json("{\"exists\": false}").allowAnyOrigin();
      }

      return HttpResponse
              .json("{\"exists\": true, \"len\": "+f.length()+", \"path\": " + StringUtil.jsonVal(ContentInfo.encodePath(f.getAbsolutePath()))
                      + ",\"dStat\":" +contentHandler.getDeviceStat().toJson() +"}")
              .allowAnyOrigin();
    }

    if ("/upload".equalsIgnoreCase(request.path())){
      String path = request.getQueryParam("file");
      final String name = request.getQueryParam("name");


      final File f = ContentInfo.fileFromPath(path);
      if (!f.exists()){
        return HttpResponse.plainText("file does not exist");
      }

      final URL url;
      try {
        url = new URL(request.getQueryParam("destination"));
      } catch (MalformedURLException e) {
        e.printStackTrace();
        return HttpResponse.plainText("invalid destination");
      }
      ThreadUtil.fireAndForget(new Runnable() {
        @Override
        public void run() {
          FileTransfer.Client client = new FileTransfer.Client();
          client.setHost(url.getHost());
          client.setPort(url.getPort());
          client.sendAndReceive(new FileTransfer.FileRequest(f, name), new CompleteHandler<HttpResponse>() {
            @Override
            public void onComplete(HttpResponse data) {
              System.out.println(data);
            }
          });
        }
      }, "WelandServerHandler#uploadHandle-" + UUID.randomUUID().toString() );

      return HttpResponse
              .json("{\"len\":" + f.length() + ", \"name\": "+StringUtil.jsonVal(name)+"}")
              .allowAnyOrigin();
    }



      //close || stopPreviews
      if (request.pathsStartsWith("files", "close")){
        contentHandler.stop(request);
        return contentHandler.getDeviceStat().toHttp(request);
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

    if ("/close-app".equals(request.path())){
      contentHandler.onCloseRequested();
      return contentHandler.getDeviceStat().toHttp();
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





}

