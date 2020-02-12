package com.arise.astox.net.servers;

import com.arise.astox.net.models.http.HttpRequestBuilder;
import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.servers.draft_6455.WSDraft6455;
import com.arise.astox.net.servers.nio.NioSslPeer;
import com.arise.core.tools.Mole;
import com.arise.core.tools.ThreadUtil;
import com.arise.weland.dto.DeviceStat;

import javax.net.ssl.SSLContext;
import java.security.SecureRandom;

public abstract class AbstractServerTest {

  private static final SSLContext context;


  static {
    String protocol = "TLSv1.2";
    SSLContext localContext = null;
    try {
      localContext = SSLContext.getInstance(protocol);
      localContext.init(
          NioSslPeer.createKeyManagers("./src/main/resources/server.jks", "storepass", "keypass"),
          NioSslPeer.createTrustManagers("./src/main/resources/trustedCerts.jks", "storepass"), new SecureRandom());
    } catch (Exception e) {
      e.printStackTrace();
      Mole.getInstance(AbstractServerTest.class).error(e);
    }
    context = localContext;
  }


  public final SSLContext getSSLContext(){
    return context;
  };


  public abstract AbstractServer serviceServer();




  public void initTest(){
   final AbstractServer server = serviceServer();

    ThreadUtil.fireAndForget(new Runnable() {
      @Override
      public void run() {
        try {
          ServerTestHandler serverTestHandler = new ServerTestHandler(context);


          DeviceStat stat = DeviceStat.getInstance();
          stat
              .setBatteryLevel(45)
              .setBatteryScale(90)
              .scanIPV4();



          server
              .addRequestBuilder(new HttpRequestBuilder())
              .addDuplexDraft(new WSDraft6455())
              .setHost("localhost")
              .setStateObserver(serverTestHandler)
              .setRequestHandler(serverTestHandler)
              .start();

          server.start();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  };

  public static void main(String[] args) {


    new IOServerTest().initTest();
    new IOSecureServerTest().initTest();
    new NIOSecureServerTest().initTest();
    new NIOServerTest().initTest();

  }
}
