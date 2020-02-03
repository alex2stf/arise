package com.arise.weland;

import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.models.ServerRequestBuilder;
import com.arise.astox.net.models.http.HttpRequestBuilder;
import com.arise.core.tools.*;
import com.arise.weland.impl.ContentInfoProvider;
import com.arise.weland.impl.DesktopFileHandler;
import com.arise.weland.impl.PCDecoder;
import com.arise.weland.impl.VLCPlayer;
import com.arise.weland.utils.Boostrap;
import com.arise.weland.utils.WelandServerHandler;

import javax.net.ssl.SSLContext;
import java.io.File;

public class Main {

    private static final SSLContext context;
    private static final Mole log = Mole.getInstance(Main.class);
    static Main corona;

    static {
//        String protocol = "TLSv1.2";
//        SSLContext localContext = null;
//        try {
//            localContext = SSLContext.getInstance(protocol);
//            localContext.init(
//                    NioSslPeer.createKeyManagers("./src/main/resources/weland/certificates/server.jks", "storepass", "keypass"),
//                    NioSslPeer.createTrustManagers("./src/main/resources/weland/certificates/trustedCerts.jks", "storepass"), new SecureRandom());
//        } catch (Exception e) {
////            e.printStackTrace();
////            Mole.getInstance(Main.class).error(e);
//        }
//        context = localContext;
        context = null;
    }

    AbstractServer server;
    private final WelandServerHandler welandServerHandler;
    Object bluetoothServer;;


    public Main(WelandServerHandler welandServerHandler) {
        this.welandServerHandler = welandServerHandler;
    }



    public static AbstractServer start(WelandServerHandler handler){

        corona = new Main(handler);
        return corona.run();
    }

    public static void stop(){
        if (corona != null){
            corona.stopAll();
        }
    }

    public static void main(String[] args) {
        VLCPlayer.getInstance();
        File roots[] = File.listRoots();


        ContentInfoProvider contentInfoProvider =
                new ContentInfoProvider(new PCDecoder())
                        .importJson("weland/config/commons/content-infos.json");

        for (File f: roots){
            if (!f.getAbsolutePath().startsWith("C:")) {
                contentInfoProvider.addRoot(f);
            }
        }
        contentInfoProvider.addRoot(new File(System.getProperty("user.home")));
//        contentInfoProvider.addRoot(new File("C:\\Users\\alexandru2.stefan\\Music"));
//        contentInfoProvider.addRoot(new File("C:\\Users\\alexandru2.stefan\\Videos"));
        contentInfoProvider.get();

        WelandServerHandler welandServerHandler = Boostrap.buildHandler(args, contentInfoProvider);
        DesktopFileHandler desktopFileHandler = new DesktopFileHandler(contentInfoProvider, Boostrap.registry);
        welandServerHandler.setContentHandler(desktopFileHandler);

        start(welandServerHandler);
    }

    public AbstractServer run(){
        server = Boostrap.startHttpServer(welandServerHandler);

        ThreadUtil.fireAndForget(new Runnable() {
            @Override
            public void run() {
                bluetoothServer = ReflectUtil.newInstance("com.arise.weland.impl.BluecoveServer");
                if (bluetoothServer != null){
                    ReflectUtil.getMethod(bluetoothServer, "setStateObserver", AbstractServer.StateObserver.class)
                            .call(welandServerHandler);

                    ReflectUtil.getMethod(bluetoothServer, "setRequestHandler", AbstractServer.RequestHandler.class)
                            .call(welandServerHandler);

                    ReflectUtil.getMethod(bluetoothServer, "addRequestBuilder", ServerRequestBuilder.class)
                            .call(new HttpRequestBuilder());

                    ReflectUtil.getMethod(bluetoothServer, "setName", String.class)
                            .call("CB_" + SYSUtils.getDeviceName() );

                    ReflectUtil.InvokeHelper invokeHelper = ReflectUtil.getMethod(bluetoothServer, "start");
                    try {
                        invokeHelper.getMethod().invoke(bluetoothServer);
                    } catch (Exception e) {
                        log.warn("Failed to start bluetooth server");
                    }
                }
            }
        });

        return server;
    }

    private void stopAll() {
        server.stop();
        if ( bluetoothServer != null){
            ReflectUtil.getMethod(bluetoothServer, "stop").call();
        }
    }

}
