package com.arise.weland.utils;


import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.models.DuplexDraft;
import com.arise.astox.net.models.ServerRequest;
import com.arise.astox.net.models.ServerResponse;
import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.astox.net.servers.HTTPServerHandler;
import com.arise.canter.CommandRegistry;
import com.arise.core.models.Handler;
import com.arise.core.models.Tuple2;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.serializers.parser.Whisker;
import com.arise.core.tools.*;
import com.arise.weland.PlaylistWorker;
import com.arise.weland.dto.*;
import com.arise.weland.impl.ContentInfoDecoder;
import com.arise.weland.impl.ContentInfoProvider;
import com.arise.weland.impl.SGService;
import com.arise.weland.model.ContentHandler;
import com.arise.weland.model.FileTransfer;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.arise.astox.net.models.http.HttpResponse.oK;
import static com.arise.core.tools.FileUtil.findStream;
import static com.arise.core.tools.StringUtil.hasText;
import static com.arise.core.tools.StringUtil.urlDecodeUTF8;
import static com.arise.core.tools.ThreadUtil.fireAndForget;
import static com.arise.core.tools.Util.now;

public class WelandServerHandler extends HTTPServerHandler {

  ContentInfoProvider contentInfoProvider;
  ContentHandler contentHandler;
  Whisker whisker = new Whisker()
          .setTemplatesRoot("src/main/resources#weland");
  String appContent = StreamUtil.toString(findStream("weland/app.html"));

  private Mole log = Mole.getInstance(WelandServerHandler.class);



  public static File getClientPropsFile(){
    return new File(FileUtil.findDocumentsDir(), "weland-client-props");
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


  public WelandServerHandler setContentHandler(ContentHandler contentHandler) {
    this.contentHandler = contentHandler;
    if (contentInfoProvider != null) {
      contentHandler.setContentInfoProvider(contentInfoProvider);
    }
    return this;
  }

  @Override
  public ServerResponse getResponse(AbstractServer serviceServer, ServerRequest request) {
    HttpRequest req = (HttpRequest) request;

    if(req.isOptions()) {
      return oK().allowAnyOrigin();
    }

    String correlationId = "";
    if (hasText(req.getHeaderParam("Correlation-Id"))){
        correlationId = req.getHeaderParam("Correlation-Id");
    }

    if ("/message".equalsIgnoreCase(req.path())
            && !"get".equalsIgnoreCase(req.method())
            && !"delete".equalsIgnoreCase(req.method())
    ){
      Map mapObj = (Map) Groot.decodeBytes(req.payload());
      Message message = Message.fromMap(mapObj);
      contentHandler.onMessageReceived(message);
      return contentHandler.getDeviceStat().toHttp();
    }

    if("/device/info".equalsIgnoreCase(req.path())){
        return HttpResponse.json(contentHandler.getDeviceInfoJson()).allowAnyOrigin();
    }


    //main html rendering
    if ("/app".equalsIgnoreCase(req.path()) || "/app.html".equalsIgnoreCase(req.path())){
      appContent = StreamUtil.toString(findStream("src/main/resources#weland/app.html"));
      Map<String, String> args = new HashMap<>();
      args.put("host", req.getQueryParamString("host", ""));
      return HttpResponse.html(whisker.compile(appContent, args));
    }


    //generic platform agnostic information
    if ("/device/stat".equals(req.path()) || "/health".equalsIgnoreCase(req.path())){
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
      simpleDateFormat.setTimeZone(TimeZone.getDefault());
      return contentHandler.getDeviceStat()
              .setProp("JT", simpleDateFormat.format(now()))
              .toHttp();
    }

    if ("/device/update".equals(req.path()) || "/health".equalsIgnoreCase(req.path())){
      return contentHandler.onDeviceUpdate(req.queryParams()).toHttp();
    }


    if ("/proxy-skin".equals(req.path()) ){
      Map<String, String> args = new HashMap<>();
      args.put("uri", urlDecodeUTF8(req.getQueryParamString("uri", "")));
      args.put("thumbnailId", req.getQueryParamString("thumbnailId", ""));
      args.put("title", urlDecodeUTF8(req.getQueryParamString("title", "")));
      return HttpResponse.html(whisker.compile(
              StreamUtil.toString(findStream("src/main/resources#weland/proxy-skin.html"))
              , args));
    }


    if (req.pathsStartsWith("device-update")){
      String what = req.getPathAt(1);
      Map<String, List<String>> params = new HashMap<>();
      String mode;

      if ("lightMode".equalsIgnoreCase(what)){
        mode = req.getPathAt(2);
        params.put("lightMode", Arrays.asList(mode));
      }
      else if ("camId".equalsIgnoreCase(what)){
        mode = req.getPathAt(2);
        params.put("camId", Arrays.asList(mode));
      }
      else if ("musicVolume".equalsIgnoreCase(what)){
        mode = req.getPathAt(2);
        params.put("musicVolume", Arrays.asList(mode));
      }
      else if ("camEnabled".equalsIgnoreCase(what)){
        mode = req.getPathAt(2);
        params.put("camEnabled", Arrays.asList(mode));
      }
      return contentHandler.onDeviceUpdate(params).toHttp();
    }



    if("/props/get".equals(req.path())){
      String key = req.getQueryParam("key");

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

    if("/props/set".equals(req.path())){
      String key = req.getQueryParam("key");
      String value = req.getQueryParam("value");

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
    if (req.pathsStartsWith("thumbnail")){
      String id = req.getQueryParam("id");
      if(StringUtil.hasContent(id)){
        return contentInfoProvider.getMediaPreview(id);
      }
      String query = req.getQueryParam("q");
      Object res = SGService.getInstance().find(query);
      if(res instanceof ServerResponse){
        return (ServerResponse) res;
      }
      return new HttpResponse().setContentType(ContentType.IMAGE_JPEG).setBytes(ContentInfoDecoder.EMPTY_BYTE);
    }


    if (req.pathsStartsWith("suggestion-uri")){
      String query = req.getQueryParam("q");
      Object response = SGService.getInstance().find(query);
      if(response instanceof ServerResponse) {
        return HttpResponse.plainText("thumbnail?id=" + query);
      }

      return HttpResponse.plainText(response +"");
    }


    if(req.pathsStartsWith("snapshot-make")){
      String id = req.getQueryParam("id");
      contentHandler.takeSnapshot(id);
      return DeviceStat.getInstance().toHttp();
    }

    //list media based on type
    if(req.pathsStartsWith("media", "list")){
      Integer index = req.getQueryParamInt("index");
      String what = req.getPathAt(2);

      Playlist playlist = Playlist.find(what);
      ContentPage page = contentInfoProvider.getPage(playlist, index);
      return HttpResponse.json(page.toString()).addCorelationId(correlationId).allowAnyOrigin();
    }




    //open given path
    if (req.pathsStartsWith("files", "open") || req.pathsStartsWith("files", "play")){
      HttpResponse response = contentHandler.openRequest(req);
      if (response != null){
        return response.allowAnyOrigin();
      }
      return DeviceStat.getInstance().toHttp(req);
    }

    //close || stopPreviews
    if (req.pathsStartsWith("files", "close")){
      HttpResponse response = contentHandler.stop(req);
      if (response != null){
        return response.allowAnyOrigin();
      }
      return contentHandler.getDeviceStat().toHttp(req);
    }

    if ("/download".equals(req.path())){
      String path = req.getQueryParam("file");
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


    if(req.pathsStartsWith("info", "persisted", "playlist", "music")){
      return HttpResponse.plainText(contentInfoProvider.getPlaylistFileContent(Playlist.MUSIC));
    }

    if(req.pathsStartsWith("transfer")){
      return HttpResponse.plainText("copied");
    }

    if ("/upload/stat".equalsIgnoreCase(req.path())){
      String fileToCheck = req.getQueryParam("name");
      File f = new File(FileUtil.getUploadDir(), fileToCheck);
      if (!f.exists()){
        return HttpResponse.json("{\"exists\": false}").allowAnyOrigin();
      }

      return HttpResponse
              .json("{\"exists\": true, \"len\": "+f.length()+", \"path\": " + StringUtil.jsonVal(ContentInfo.encodePath(f.getAbsolutePath()))
                      + ",\"dStat\":" +contentHandler.getDeviceStat().toJson() +"}")
              .allowAnyOrigin();
    }

    if ("/upload".equalsIgnoreCase(req.path())){
      String path = req.getQueryParam("file");
      final String name = req.getQueryParam("name");


      final File f = ContentInfo.fileFromPath(path);
      if (!f.exists()){
        return HttpResponse.plainText("file does not exist");
      }

      final URL url;
      try {
        url = new URL(req.getQueryParam("destination"));
      } catch (MalformedURLException e) {
        e.printStackTrace();
        return HttpResponse.plainText("invalid destination");
      }
      fireAndForget(new Runnable() {
        @Override
        public void run() {
          FileTransfer.Client client = new FileTransfer.Client();
          client.setHost(url.getHost());
          client.setPort(url.getPort());
          client.sendAndReceive(new FileTransfer.FileRequest(f, name), new Handler<HttpResponse>() {
            @Override
            public void handle(HttpResponse data) {
              System.out.println(data);
            }
          });
        }
      }, "WelandServerHandler#uploadHandle-" + UUID.randomUUID().toString() );

      return HttpResponse
              .json("{\"len\":" + f.length() + ", \"name\": "+StringUtil.jsonVal(name)+"}")
              .allowAnyOrigin();
    }





     //PAUSE
      if (req.pathsStartsWith("media", "pause")){
          HttpResponse response =  contentHandler.pauseRequest(req);
          if (response != null){
              return response.allowAnyOrigin();
          }
          return DeviceStat.getInstance().toHttp(req);
      }

      //package-info loader
      if (req.pathsStartsWith("pack")){
        String path = req.getQueryParam("root");
        File f = new File(path);
        try {
          Map props = ContentInfoProvider.packageInfoProps(f);
          File html = new File(f.getParentFile(), MapUtil.getString(props, "main"));
          return HttpResponse.html(FileUtil.read(html));
        } catch (IOException e) {
          e.printStackTrace();
          return oK();
        }

      }

    if ("/ping".equals(req.path())){
      HttpResponse serverResponse = oK();
      for (Map.Entry<String, String> entry : req.getHeaders().entrySet()){
        serverResponse.addHeader(entry.getKey() + "-Response" , entry.getValue() + "-Response");
      }
      serverResponse.setText("PONG").addCorelationId(correlationId).allowAnyOrigin();
      return DeviceStat.getInstance().toHttp(req);
    }






    if (req.pathsStartsWith("playlist")){
      String action = req.getQueryParamString("action", "xx");
      String name = req.getQueryParamString("name", null);
      String path = req.getQueryParamString("path", null);
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

    if ("/close-app".equals(req.path())){
      contentHandler.onCloseRequested();
      return contentHandler.getDeviceStat().toHttp();
    }

    if (req.pathsStartsWith("commands", "registry")){
      return HttpResponse.json(CommandRegistry.getInstance().toString());
    }

    if (req.pathsStartsWith("commands", "exec")){
      String commandId = req.getQueryParam("cmd");
      String[] args = req.getQueryParamList("args");
      Object o = CommandRegistry.getInstance().execute(commandId, args, null, null);
      return HttpResponse.plainText(String.valueOf(o));
    }


    return null;
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





}

