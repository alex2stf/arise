package com.arise.weland.impl;

import com.arise.astox.net.models.ServerResponse;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.canter.CommandRegistry;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.*;
import com.arise.weland.dto.ContentInfo;

import java.io.*;
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

    public static void setDesktopImage(String desktopImage) {
        Object img = getInstance().find(desktopImage);
        if(img == null) {
            int rand = (int) Math.round((Math.random() * 7) + 0);
            try {
                img = new HttpResponse().setBytes(
                        StreamUtil.toBytes(FileUtil.findStream("pictures/desk" + rand + ".jpg"))
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File out = new File(FileUtil.findPicturesDir(), "arise-desktop.png");
        File tmp = FileUtil.findSomeTempFile("tmp_desk");
        if(tmp.exists()){
            tmp.delete();
        }

        if(img instanceof HttpResponse) {
            HttpResponse res = (HttpResponse) img;
            FileUtil.writeBytesToFile(res.bodyBytes(), tmp);
        }
        img = "https://cache.desktopnexus.com/cropped-wallpapers/2215/2215579-1536x864-[DesktopNexus.com].jpg";

        if(img instanceof String) {
            String x = (String) img;
            if(x.startsWith("http")) {
                try {
                    Object p = CommandRegistry.getInstance().getCommand("process-exec")
                            .execute("curl", x, ">", tmp.getAbsolutePath());
                    if(p instanceof Process){
                        ((Process)p).waitFor();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if(tmp.exists() && CommandRegistry.getInstance().containsCommand("set-desktop-background")) {
            CommandRegistry.getInstance().execute("set-desktop-background", new String[]{tmp.getAbsolutePath(), out.getAbsolutePath(), new Date()+ "" });
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

//    private Map getSuggestion(String  val){
//        List suggestions = MapUtil.getList(root, "suggestions");
//        if (suggestions == null){
//            return null;
//        }
//        for (Object o: suggestions){
//            if (o instanceof Map){
//                Map m = (Map) o;
//                if (m.containsKey("key")){
//                    if (String.valueOf(m.get("key")).equalsIgnoreCase(val)){
//                        return m;
//                    }
//                }
//            }
//        }
//        return null;
//    }




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
