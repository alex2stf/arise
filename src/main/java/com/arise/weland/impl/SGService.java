package com.arise.weland.impl;

import com.arise.astox.net.models.ServerResponse;
import com.arise.astox.net.models.http.HttpResponse;
import com.arise.canter.CommandRegistry;
import com.arise.core.models.Handler;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.*;
import com.arise.weland.dto.ContentInfo;

import java.io.*;
import java.util.*;

import static com.arise.core.tools.CollectionUtil.*;
import static com.arise.core.tools.TypeUtil.isNull;
import static com.arise.core.tools.TypeUtil.search;

public enum  SGService {
    INSTANCE;


    public static final SGService getInstance(){
        return INSTANCE;
    }
    Map<String, Set<String>> root = new HashMap();

    private static final Mole log = Mole.getInstance("SGSERVICE");

    private static final List<String> urls = new ArrayList<>();
    private static final int [] urlIndex = new int[]{0};
    static {
        StreamUtil.readLineByLine(FileUtil.findStream("pictures/images.txt"), new StreamUtil.LineIterator() {
            @Override
            public void onLine(int lineNo, String content) {
                urls.add(content);
            }
        });
        Collections.shuffle(urls);
    }

    static File tmpDesk(){
        return FileUtil.findSomeTempFile("tmp_desk");
    }

    static void scrieCevaDinLocal(){
        int rand = (int) Math.round((Math.random() * 7) + 0);
        String name = "pictures/desk" + rand + ".jpg";
        try {
            StreamUtil.transfer(
                    FileUtil.findStream(name),
                    new FileOutputStream(tmpDesk())
            );
        } catch (IOException e) {
            log.error("Nu am putut copia " + name + " in " + tmpDesk().getAbsolutePath(), e);
        }
    }



    static void downloadImage(final String x, Handler<Object> onErr) {
        if(!x.startsWith("http")) {
            onErr.handle(null);
            return;
        }

        try {
            FileUtil.findSomeTempFile("tmp_desk").delete();
            NetworkUtil.downloadImage(x, FileUtil.findSomeTempFile("tmp_desk"));
        } catch (Exception e) {
            e.printStackTrace();
            onErr.handle(null);
        }
    }

    public static synchronized void setDesktopImage(String desktopImage) {
        if(!CommandRegistry.getInstance().containsCommand("set-desktop-background")){
            log.warn("Nu poti seta desktop fara comanda definita");
            return;
        }

        tmpDesk().delete();
        Object image = getInstance().find(desktopImage);

        if(null == image) {
            log.info("Nu s-a gasit nici o sugestie pentru " + desktopImage);
            iaDefaultDinUrl();  //returneaza HttpResponse sau url pe imgs[0]
        }

        File out = new File(FileUtil.findPicturesDir(), "arise-desktop.png");
        if (image instanceof HttpResponse){
            HttpResponse res = (HttpResponse) image;
            FileUtil.writeBytesToFile(res.bodyBytes(), tmpDesk());
        } else if(image instanceof String) {
            final String x = (String) image;
            downloadImage(x, new Handler<Object>() {
                @Override
                public void handle(Object o) {
                    iaDefaultDinUrl(); //returneaza HttpResponse sau url pe imgs[0]
                }
            });
        }

        //a doua iteratie
        if(!tmpDesk().exists()){
            scrieCevaDinLocal();
        }


        if(tmpDesk().exists()) {
            String title = ContentInfoProvider.findTitle(desktopImage);
            if(!StringUtil.hasText(title)){
                title = desktopImage;
            }
            CommandRegistry.getInstance().execute("set-desktop-background", new String[]{
                    tmpDesk().getAbsolutePath(),
                    out.getAbsolutePath(),
                    title
            });
        } else {
			System.out.println("NU EXISTA TMP-UL");
		}

    }


    public static void iaDefaultDinUrl(){
        if(urlIndex[0] > urls.size() - 1) {
            urlIndex[0] = 0;
        }
        final String nextUrl= urls.get(urlIndex[0]);
        try {
            NetworkUtil.downloadImage(nextUrl, tmpDesk());
            urlIndex[0]++;
            log.info("E ok " + nextUrl);
        } catch (Exception e) {
            log.info("Nu e ok " + nextUrl);
            urlIndex[0]++;
            if(urlIndex[0] > urls.size() - 1) {
                urlIndex[0] = 0;
                log.info("Am terminat de iterat ");
                scrieCevaDinLocal();
            } else {
                iaDefaultDinUrl();
            }
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




//    char disallowed[] = new char[]{'~', '-'};

//    private boolean isValidWord(String s){
//        if (isNull(s)){
//            return false;
//        }
//        if (s.length() == 1) {
//            for (char c: disallowed){
//                if (c == s.charAt(0)){
//                    return false;
//                }
//            }
//        }
//        return true;
//    }



//    private List<String> getLinearCombinations(List<String> vals) {
//
//        List<String> r = new ArrayList<>();
//        if (vals.size() == 1){
//            r.add(vals.get(0));
//            r.add(vals.get(0).toLowerCase());
//            return r;
//        }
//        for (int j = 0; j < vals.size(); j++){
//            String c = vals.get(j);
//            for (int i = j + 1; i < vals.size(); i++){
//                r.add(c + " " + vals.get(i));
//                c+=" " + vals.get(i);
//            }
//        }
//        Collections.sort(r, new Comparator<String>() {
//            @Override
//            public int compare(String s, String t1) {
//                return Integer.compare(s.length(), t1.length());
//            }
//        });
//        Collections.reverse(r);
//        return r;
//    }

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
            //TODO reconcateneaza perechi, nu doar primele 2
            if (parts.length >= 2){
                root.put(parts[0] + " " + parts[1], icons);
                root.put(parts[0] + parts[1], icons);
            } else {
                root.put(id, icons);
            }

        } catch (Exception e){

        }
        root.put(id, icons);
        root.put(id.replaceAll("\\s+", ""), icons);



        if(path != null && !ContentType.isBase64EncodedImage(path)){
            root.put(path.toLowerCase(), icons);
            root.put(path.toLowerCase().replaceAll("\\s+", ""), icons);
        }
    }


    public synchronized Object find(String id) {
        id = id.toLowerCase();
        if(root.containsKey(id)) {
            String path = randomPickElement(root.get(id));
            return decodePath(path);

        }

//        System.out.println("CAUTA " + id);
        //idul devine query
        String query = id;
        for (String key: root.keySet()){
            if (query.toLowerCase().indexOf(key.toLowerCase()) > -1){
                return decodePath(randomPickElement(root.get(key)));
            }
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



}
