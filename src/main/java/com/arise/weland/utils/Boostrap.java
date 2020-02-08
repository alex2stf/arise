package com.arise.weland.utils;

import com.arise.astox.net.models.AbstractServer;
import com.arise.astox.net.models.http.HttpRequest;
import com.arise.astox.net.models.http.HttpRequestBuilder;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.astox.net.servers.draft_6455.WSDraft6455;
import com.arise.astox.net.servers.io.IOServer;
import com.arise.canter.Command;
import com.arise.canter.Registry;
import com.arise.core.tools.ContentType;
import com.arise.core.tools.Mole;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.ThreadUtil;
import com.arise.weland.impl.ContentInfoProvider;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.arise.canter.Defaults.PROCESS_EXEC;
import static com.arise.canter.Defaults.PROCESS_EXEC_WHEN_FOUND;

public class Boostrap {
    private static final Mole log = Mole.getInstance(Boostrap.class);
    public static final Registry registry = new Registry();
    public static WelandServerHandler buildHandler(String[] args, ContentInfoProvider contentInfoProvider){
        try {
            ContentType.loadDefinitions();
            log.info("Successfully loaded content-type definitions");
        } catch (Exception e){
            log.error("Failed to load content-type definitions", e);
        }


        registry.addCommand(PROCESS_EXEC)
                .addCommand(PROCESS_EXEC_WHEN_FOUND);

        try {
            registry.loadJsonResource("src/main/resources#/weland/config/commons/commands.json");
            if (SYSUtils.isWindows()){
                registry.loadJsonResource("src/main/resources#/weland/config/win/commands.json");
            } else {
                registry.loadJsonResource("src/main/resources#/weland/config/unix/commands.json");
            }
            log.info("Successfully loaded commands definitions");
        } catch (Exception e){
            log.error("Failed to load commands definitions", e);
        }

//        Object decoder = ReflectUtil.newInstance("com.arise.weland.impl.PCDecoder");

//        ContentInfoProvider
        WelandServerHandler welandServerHandler = new WelandServerHandler()
                                                    .setContentProvider(contentInfoProvider);



        welandServerHandler.onCommandExecRequest(new WelandServerHandler.Handler<HttpRequest>() {
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
                    args = new String[]{ URLBeautifier.beautify(args[0])};
                }

                return HttpResponse.json(
                        registry.getCommand(request.pathAt(2)).execute(args).toString()
                );
            }
        });


        return welandServerHandler;
    }


    public static AbstractServer startHttpServer(final WelandServerHandler welandServerHandler){
        final AbstractServer server = new IOServer()
                .setPort(8221)
                .setName("DR_" + SYSUtils.getDeviceName())
                .setUuid(UUID.randomUUID().toString());

        ThreadUtil.fireAndForget(new Runnable() {
            @Override
            public void run() {
                try {
                    server
                            .addRequestBuilder(new HttpRequestBuilder())
                            .addDuplexDraft(new WSDraft6455())
                            .setHost("localhost")
                            .setStateObserver(welandServerHandler)
                            .setRequestHandler(welandServerHandler)
                            .start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return server;
    }
}
