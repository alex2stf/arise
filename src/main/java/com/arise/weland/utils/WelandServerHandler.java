package com.arise.weland.utils;


import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.models.ServerRequest;
import com.arise.astox.net.models.ServerResponse;
import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.astox.net.servers.HTTPServerHandler;
import com.arise.canter.CommandRegistry;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.serializers.parser.Whisker;
import com.arise.core.tools.*;
import com.arise.weland.PlaylistWorker;
import com.arise.weland.dto.*;
import com.arise.weland.impl.ContentInfoDecoder;
import com.arise.weland.impl.ContentInfoProvider;
import com.arise.weland.impl.SGService;
import com.arise.weland.model.ContentHandler;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.arise.astox.net.models.http.HttpResponse.oK;
import static com.arise.core.tools.FileUtil.findStream;
import static com.arise.core.tools.StringUtil.hasText;
import static com.arise.core.tools.StringUtil.urlDecodeUTF8;
import static com.arise.core.tools.Util.now;

public class WelandServerHandler extends HTTPServerHandler {

  ContentInfoProvider contentInfoProvider;
  ContentHandler contentHandler;
  Whisker whisker = new Whisker()
          .setTemplatesRoot("src/main/resources#weland");
  String appContent = StreamUtil.toString(findStream("weland/app.html"));

  private Mole log = Mole.getInstance(WelandServerHandler.class);










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
    AppDispatcher.tick();

    if(req.isOptions()) {
      return oK().allowAnyOrigin();
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



    if (req.pathsStartsWith("device-update")){
      String what = req.getPathAt(1);
      Map<String, List<String>> params = new HashMap<>();
      String value;

      if ("musicVolume".equalsIgnoreCase(what)){
        value = req.getPathAt(2);
        params.put("musicVolume", Arrays.asList(value));
      }

      return contentHandler.onDeviceUpdate(params).toHttp();
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
      String query = req.getQueryParam("q").toLowerCase();
      Object response = SGService.getInstance().find(query);
      if(response instanceof ServerResponse) {
        return HttpResponse.plainText("thumbnail?id=" + query);
      }
//      System.out.println("NU AM GASIT NIMIC PENTRU " + query);

      return HttpResponse.plainText(response +"");
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




     //PAUSE
      if (req.pathsStartsWith("media", "pause")){
          HttpResponse response =  contentHandler.pauseRequest(req);
          if (response != null){
              return response.allowAnyOrigin();
          }
          return DeviceStat.getInstance().toHttp(req);
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


    appContent = StreamUtil.toString(findStream("src/main/resources#weland/app.html"));
    Map<String, String> args = new HashMap<>();
    args.put("host", req.getQueryParamString("host", ""));
    args.put("osName", DeviceStat.getInstance().getOs().qualifiedName());
    args.put("deviceName", DeviceStat.getInstance().getDisplayName());
    return HttpResponse.html(whisker.compile(appContent, args));
  }






}

