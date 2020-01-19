package com.arise.corona;

import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.models.ServerRequestBuilder;
import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpRequestBuilder;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.astox.net.servers.draft_6455.WSDraft6455;
import com.arise.astox.net.servers.io.IOServer;
import com.arise.canter.Command;
import com.arise.canter.Registry;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.Mole;
import com.arise.core.tools.ReflectUtil;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.ThreadUtil;
import com.arise.corona.impl.IDeviceController;
import com.arise.corona.utils.CoronaServerHandler;
import com.arise.corona.utils.URLBeautifier;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.SSLContext;

import static com.arise.canter.Defaults.PROCESS_EXEC;
import static com.arise.canter.Defaults.PROCESS_EXEC_WHEN_FOUND;

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

    final AbstractServer server = new IOServer();
    private final CoronaServerHandler coronaServerHandler;
    Object bluetoothServer;;


    public Main(CoronaServerHandler coronaServerHandler) {
        this.coronaServerHandler = coronaServerHandler;
    }

    public static CoronaServerHandler buildHandler(String[] args){
        try {
            ContentType.loadDefinitions();
            log.info("Successfully loaded content-type definitions");
        } catch (Exception e){
            log.error("Failed to load content-type definitions", e);
        }

        Registry registry = new Registry();
        registry.addCommand(PROCESS_EXEC)
                .addCommand(PROCESS_EXEC_WHEN_FOUND);

        try {
            registry.loadJsonResource("src/main/resources#/corona/config/commons/commands.json");
            if (SYSUtils.isWindows()){
                registry.loadJsonResource("src/main/resources#/corona/config/win/commands.json");
            } else {
                registry.loadJsonResource("src/main/resources#/corona/config/unix/commands.json");
            }
            log.info("Successfully loaded commands definitions");
        } catch (Exception e){
            log.error("Failed to load commands definitions", e);
        }

        CoronaServerHandler coronaServerHandler = new CoronaServerHandler();

        coronaServerHandler.onFileOpenRequest(new CoronaServerHandler.Handler<HttpRequest>() {
            @Override
            public HttpResponse handle(HttpRequest request) {
                SYSUtils.open(request.getQueryParam("path"));
                return null;
            }
        });

        coronaServerHandler.onCommandExecRequest(new CoronaServerHandler.Handler<HttpRequest>() {
            @Override
            public HttpResponse handle(HttpRequest request) {
                Command command = registry.getCommand(request.pathAt(2));
                if (command == null){
                    return HttpResponse.plainText(request.pathAt(2) + " cmd not found");
                }
                String[] args = new String[request.getQueryParams().size()];
                int i = 0;
                for (Map.Entry<String, List<String>> entry: request.getQueryParams().entrySet() ){
                    args[i] = entry.getValue().get(0);
                    i++;
                }

                if ("browserOpen".equalsIgnoreCase(command.getId())){
                    args = URLBeautifier.beautify(args);
                }

                return HttpResponse.json(
                        registry.getCommand(request.pathAt(2)).execute(args).toString()
                );
            }
        });


        if (ReflectUtil.classExists("com.arise.corona.impl.PCDeviceController")){
            IDeviceController iDeviceController =
                    (IDeviceController) ReflectUtil.newInstance("com.arise.corona.impl.PCDeviceController");
            coronaServerHandler.onDeviceControlsUpdate(new CoronaServerHandler.Handler<HttpRequest>() {
                @Override
                public HttpResponse handle(HttpRequest request) {
                    iDeviceController.update(request.getQueryParams());
                    return null;
                }
            });
        }

        return coronaServerHandler;
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
        start(
                buildHandler(args)
        );
    }

    public AbstractServer run(){
        server.setPort(8221)
               .setName("CS_" + SYSUtils.getDeviceName())
                .setUuid(UUID.randomUUID().toString());




        ThreadUtil.fireAndForget(new Runnable() {
            @Override
            public void run() {
                try {
                    server
                            .addRequestBuilder(new HttpRequestBuilder())
                            .addDuplexDraft(new WSDraft6455())
                            .setHost("localhost")
                            .setStateObserver(coronaServerHandler)
                            .setRequestHandler(coronaServerHandler)
                            .start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

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
