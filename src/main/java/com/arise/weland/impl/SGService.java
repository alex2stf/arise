package com.arise.weland.impl;

import com.arise.astox.net.models.Peer;
import com.arise.astox.net.models.ServerResponse;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.canter.CommandRegistry;
import com.arise.core.models.Handler;
import com.arise.core.models.Tuple2;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.*;
import com.arise.weland.dto.ContentInfo;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.*;

import static com.arise.core.tools.CollectionUtil.isEmpty;
import static com.arise.core.tools.CollectionUtil.merge;
import static com.arise.core.tools.TypeUtil.isNull;
import static com.arise.core.tools.TypeUtil.search;

public enum  SGService {
    INSTANCE;


    public static final SGService getInstance(){
        return INSTANCE;
    }
    Map<String, Set<String>> root = new HashMap();
    private SGCache cacheStrategy;

    private static final Mole log = Mole.getInstance(SGService.class);

    private static final List<String> urls = new ArrayList<>();
    static {
        urls.add("https://live.staticflickr.com/65535/52607511849_af34b6e5d9.jpg");
        urls.add("https://mcdn.wallpapersafari.com/medium/40/55/gtBrSV.jpg");
        urls.add("https://i.ytimg.com/vi/bhc7y-n7vIU/hqdefault.jpg");
        urls.add("https://images.fineartamerica.com/images/artworkimages/mediumlarge/1/in-the-end-swedish-attitude-design.jpg");
        urls.add("https://1.bp.blogspot.com/_SWYwL3fIkFs/S95uhByssMI/AAAAAAAAEp8/FXftVrz7Ii4/s1600/oil+painting+abstract+windows+media+player+skin.png");
    }

    static void citesteDefaultDinLocal(Object imgs[]){
        int rand = (int) Math.round((Math.random() * 7) + 0);
        try {
            imgs[0] = new HttpResponse().setBytes(
                    StreamUtil.toBytes(FileUtil.findStream("pictures/desk" + rand + ".jpg"))
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setDesktopImage(String desktopImage) {
        final Object imgs[] = new Object[]{getInstance().find(desktopImage)};



        if(null == imgs[0]) {
            final String nextUrl = CollectionUtil.randomPick(urls);
            RadioPlayer.getMediaPlayer().validateStreamUrl(nextUrl, new Handler<HttpURLConnection>() {
                @Override
                public void handle(HttpURLConnection httpURLConnection) {
                    imgs[0]= nextUrl;
                }
            }, new Handler<Tuple2<Throwable, Peer>>() {
                @Override
                public void handle(Tuple2<Throwable, Peer> throwablePeerTuple2) {
                    log.error("Nu ai conexiune la net, nu se poate citi " + nextUrl);
                    citesteDefaultDinLocal(imgs);
                }
            });
        }



        File out = new File(FileUtil.findPicturesDir(), "arise-desktop.png");
        final File[] tmp = {FileUtil.findSomeTempFile("tmp_desk")};
        if(tmp[0].exists()){
            tmp[0].delete();
        }


        if(imgs[0] instanceof HttpResponse) {
            HttpResponse res = (HttpResponse) imgs[0];
            FileUtil.writeBytesToFile(res.bodyBytes(), tmp[0]);
        }
        

        if(imgs[0] instanceof String) {
            final String x = (String) imgs[0];
            if(x.startsWith("http")) {
                RadioPlayer.getMediaPlayer().validateStreamUrl(x, new Handler<HttpURLConnection>() {
                    @Override
                    public void handle(HttpURLConnection httpURLConnection) {
                        try {
                            Object p = CommandRegistry.getInstance().getCommand("process-exec")
                                    .execute("curl", x, "-o", tmp[0].getAbsolutePath());
                            if(p instanceof Process){
                                ((Process)p).waitFor();
                            }
                            tmp[0] = FileUtil.findSomeTempFile("tmp_desk");
                        } catch (Exception e) {
                            e.printStackTrace();
                            citesteDefaultDinLocal(imgs);
                        }
                    }
                }, new Handler<Tuple2<Throwable, Peer>>() {
                    @Override
                    public void handle(Tuple2<Throwable, Peer> throwablePeerTuple2) {
                        log.error("Exista sugestie valid definita dar nu exista conexiune la internet");
                        citesteDefaultDinLocal(imgs);
                    }
                });
            }
        }

        if(tmp[0].exists() && CommandRegistry.getInstance().containsCommand("set-desktop-background")) {
            CommandRegistry.getInstance().execute("set-desktop-background", new String[]{tmp[0].getAbsolutePath(), out.getAbsolutePath(), desktopImage });
        } else {
			System.out.println("NU EXISTA TMP-UL");
		}

    }


    public SGService  load(String path){
        InputStream inputStream = FileUtil.findStream(path);
        if (inputStream == null){
            log.error("stream " + path + "NOT FOUND");
            return this;
        }

        String cnt = StreamUtil.toString(inputStream).replaceAll("\r\n", " ");

        Map<Object, Object> local = (Map<Object, Object> ) Groot.decodeBytes(cnt);
        if (local == null){
            return this;
        }
        List suggestions = (List) local.get("suggestions");

        for(Object item: suggestions) {
            if(item instanceof Map) {
                Map suggestion = (Map) item;
                String key = (String) suggestion.get("key");
                List<String> icons  = (List<String>) suggestion.get("icons");

                Set<String> set = root.containsKey(key) ? root.get(key) : new HashSet<String>();
                for(String x: icons) {
                    set.add(x);
                }
                addVariants(key, set, null);
            }
        }

        return this;
    }





    private static final String images_extension[] = new String[]{"png", "jpeg", "jpg"};








    public static ServerResponse decodeBase64Image(String input) throws Exception {
        int sepIndex = input.indexOf(",");
        String start = input.substring(0, sepIndex);
        String ctype = start.substring(start.indexOf(":") + 1, start.indexOf(";"));
        ContentType contentType = ContentType.search(ctype);
        String content = input.substring(sepIndex + 1);
        byte[] bytes =   B64.decodeToByteArray(content);;
        return new HttpResponse().setBytes(bytes).setContentType(contentType);
    }




    char disallowed[] = new char[]{'~', '-'};

    private boolean isValidWord(String s){
        if (isNull(s)){
            return false;
        }
        if (s.length() == 1) {
            for (char c: disallowed){
                if (c == s.charAt(0)){
                    return false;
                }
            }
        }
        return true;
    }



    private List<String> getLinearCombinations(List<String> vals) {

        List<String> r = new ArrayList<>();
        if (vals.size() == 1){
            r.add(vals.get(0));
            r.add(vals.get(0).toLowerCase());
            return r;
        }
        for (int j = 0; j < vals.size(); j++){
            String c = vals.get(j);
            for (int i = j + 1; i < vals.size(); i++){
                r.add(c + " " + vals.get(i));
                c+=" " + vals.get(i);
            }
        }
        Collections.sort(r, new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return Integer.compare(s.length(), t1.length());
            }
        });
        Collections.reverse(r);
        return r;
    }

    public String createThumbnailId(ContentInfo contentInfo, String path) {
        String id = StringUtil.sanitizeAppId(contentInfo.getPath()); //TODO fa-o id
        Set<String> icons = root.get(id);
        if(CollectionUtil.isEmpty(icons)){
            icons = new HashSet<>();
        }
        icons.add(path);
        addVariants(id, icons, path);
        if(StringUtil.hasContent(contentInfo.getTitle()) && null != contentInfo.getTitle()){
            addVariants(contentInfo.getTitle(), icons, null);
        }
        return id;
    }

    void addVariants(String id, Set<String> icons, String path) {
        id = id.toLowerCase().replaceAll("\\s+", " "); //remove tabs and newlines
        try {
            String[] parts = id.split(" ");
            if (parts.length > 2){
                root.put(parts[0] + " " + parts[1], icons);
                root.put(parts[0] + parts[1], icons);
            }

        }catch (Exception e){

        }
        root.put(id, icons);
        root.put(id.replaceAll("\\s+", ""), icons);



        if(path != null && !ContentType.isBase64EncodedImage(path)){
            root.put(path.toLowerCase(), icons);
            root.put(path.toLowerCase().replaceAll("\\s+", ""), icons);
        }
    }

    public Object find(String id) {
        id = id.toLowerCase();
        if(root.containsKey(id)) {
            String path = root.get(id).iterator().next();  //TODO pick next sau random
            return decodePath(path);

        }
        //idul devine query
        String query = id;
        for (String key: root.keySet()){
            if (query.toLowerCase().indexOf(key.toLowerCase()) > -1){
                return decodePath(root.get(key).iterator().next()); //TODO pick next
            }
        }
        if(id.indexOf("bob dylan") > -1){
            System.out.println("TODO poti facse scrap web for ??? " + id);
        }


        return null;
    }


    private Object decodePath(String path){
        //citeste din classpath
        if(path.startsWith("classpath:")) {
            path = path.substring("classpath:".length());
            try {
                return new HttpResponse()
                        .setBytes(StreamUtil.toBytes(FileUtil.findStream(path)))
                        .setContentType(ContentType.search(path));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        //decode base64
        else if(path.startsWith("data:image/")){
            try {
                return decodeBase64Image(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        else if(ContentType.isHttpPath(path)) {
            return path;
        }
        //TODO aici poti face scrapping dupa url sau numele fisierului
        System.out.println("TODO scrap web for ????? " + path);
        return path;
    }



    public static class SuggestionFetchException extends RuntimeException {

        private final String m;
        private final Throwable c;

        public SuggestionFetchException(String m, Throwable c){
            super(m, c);
            this.m = m;
            this.c = c;
        }


    }

}
