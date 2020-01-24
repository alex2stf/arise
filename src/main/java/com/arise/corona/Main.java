package com.arise.corona;

import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.models.ServerRequestBuilder;
import com.arise.astox.net.models.http.HttpRequestBuilder;
import com.arise.core.tools.Mole;
import com.arise.core.tools.ReflectUtil;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.ThreadUtil;
import com.arise.corona.impl.ContentInfoProvider;
import com.arise.corona.impl.PCDecoder;
import com.arise.corona.utils.Boostrap;
import com.arise.corona.utils.CoronaServerHandler;

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
//                    NioSslPeer.createKeyManagers("./src/main/resources/corona/certificates/server.jks", "storepass", "keypass"),
//                    NioSslPeer.createTrustManagers("./src/main/resources/corona/certificates/trustedCerts.jks", "storepass"), new SecureRandom());
//        } catch (Exception e) {
////            e.printStackTrace();
////            Mole.getInstance(Main.class).error(e);
//        }
//        context = localContext;
        context = null;
    }

    AbstractServer server;
    private final CoronaServerHandler coronaServerHandler;
    Object bluetoothServer;;


    public Main(CoronaServerHandler coronaServerHandler) {
        this.coronaServerHandler = coronaServerHandler;
    }



    public static AbstractServer start(CoronaServerHandler handler){

        corona = new Main(handler);
        return corona.run();
    }

    public static void stop(){
        if (corona != null){
            corona.stopAll();
        }
    }

    public static void main(String[] args) {
        ContentInfoProvider contentInfoProvider =
                new ContentInfoProvider(new PCDecoder())
                        .addRoot(new File("C:\\Users\\alexandru2.stefan\\Music"))
                .get();
        start(
                Boostrap.buildHandler(args, contentInfoProvider)
        );
    }

    public AbstractServer run(){
        server = Boostrap.startHttpServer(coronaServerHandler);

        ThreadUtil.fireAndForget(new Runnable() {
            @Override
            public void run() {
                bluetoothServer = ReflectUtil.newInstance("com.arise.corona.impl.BluecoveServer");
                if (bluetoothServer != null){
                    ReflectUtil.getMethod(bluetoothServer, "setStateObserver", AbstractServer.StateObserver.class)
                            .call(coronaServerHandler);

                    ReflectUtil.getMethod(bluetoothServer, "setRequestHandler", AbstractServer.RequestHandler.class)
                            .call(coronaServerHandler);

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
