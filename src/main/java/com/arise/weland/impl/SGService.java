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
import java.net.URLConnection;
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
    private SGCache cacheStrategy;

    private static final Mole log = Mole.getInstance(SGService.class);

    private static final List<String> urls = new ArrayList<>();
    private static final int [] urlIndex = new int[]{0};
    static {
        urls.add("https://i.pinimg.com/originals/43/8f/a8/438fa8f38d01e429201126e13c8015df.jpg");
        urls.add("https://i.pinimg.com/originals/9b/fd/a0/9bfda0efb535e51570c6648a41e7a3c8.jpg");
        urls.add("https://getwallpapers.com/wallpaper/full/0/2/0/1410169-most-popular-blues-music-wallpaper-1920x1200-for-meizu.jpg");
        urls.add("https://mcdn.wallpapersafari.com/medium/35/85/WFlGVS.jpg");
        urls.add("https://www.desktopbackground.org/p/2015/09/29/1018545_download-10-stunning-sexy-ubuntu-wallpapers_1920x1200_h.jpg");
        urls.add("https://www.free-codecs.com/pictures/screenshots/bsplayer.jpg");
        urls.add("https://media-assets.wired.it/photos/615f319b5b820b8db3e7cd47/master/w_1600%2Cc_limit/1518175196_Winamp.jpg");
        urls.add("https://media.askvg.com/articles/images5/Winamp_Skin_VLC_Media_Player.png");
        urls.add("https://wmpskinsarchive.neocities.org/images/tubeframe.png");
        urls.add("https://wmpskinsarchive.neocities.org/images/STALKER.png");
        urls.add("https://e0.pxfuel.com/wallpapers/150/412/desktop-wallpaper-sexy-in-black-sexy-lingerie-model-woman.jpg");
        urls.add("https://wmpskinsarchive.neocities.org/images/QuickSilver.png");
        urls.add("https://wmpskinsarchive.neocities.org/images/Main_Street.png");
        urls.add("https://m.media-amazon.com/images/I/71FydjvsrcL._AC_SX679_.jpg");
        urls.add("https://getwallpapers.com/wallpaper/full/d/c/8/1410156-widescreen-blues-music-wallpaper-1920x1200-samsung-galaxy.jpg");
        urls.add("https://wmpskinsarchive.neocities.org/images/digitaldj.png");
        urls.add("https://w0.peakpx.com/wallpaper/495/184/HD-wallpaper-sexy-female-art-art-female-model-abstract-sexy.jpg");
        urls.add("https://wmpskinsarchive.neocities.org/images/cerulean.png");
        urls.add("https://wmpskinsarchive.neocities.org/images/9SeriesDefault.png");
        urls.add("https://w.wallhaven.cc/full/42/wallhaven-42k88m.jpg");
        urls.add("https://wmpskinsarchive.neocities.org/images/ALXVortex.png");
        urls.add("https://live.staticflickr.com/65535/52607511849_af34b6e5d9.jpg");
        urls.add("https://mcdn.wallpapersafari.com/medium/40/55/gtBrSV.jpg");
        urls.add("https://i.ytimg.com/vi/bhc7y-n7vIU/hqdefault.jpg");
        urls.add("https://getwallpapers.com/wallpaper/full/4/1/5/1410163-large-blues-music-wallpaper-1920x1200.jpg");
        urls.add("https://getwallpapers.com/wallpaper/full/e/6/0/1410148-download-blues-music-wallpaper-1920x1080-ipad-pro.jpg");
        urls.add("https://getwallpapers.com/wallpaper/full/9/d/4/1410145-blues-music-wallpaper-1920x1080-windows-7.jpg");
        urls.add("https://getwallpapers.com/wallpaper/full/f/c/b/1410170-full-size-blues-music-wallpaper-2560x1600-ios.jpg");
        urls.add("https://images.fineartamerica.com/images/artworkimages/mediumlarge/1/in-the-end-swedish-attitude-design.jpg");
        urls.add("https://1.bp.blogspot.com/_SWYwL3fIkFs/S95uhByssMI/AAAAAAAAEp8/FXftVrz7Ii4/s1600/oil+painting+abstract+windows+media+player+skin.png");
        Collections.shuffle(urls);
    }

    static void citesteDefaultDinLocal(Object imgs[]){
        int rand = (int) Math.round((Math.random() * 7) + 0);
        try {
            imgs[0] = new HttpResponse().setBytes(
                    StreamUtil.toBytes(FileUtil.findStream("pictures/desk" + rand + ".jpg"))
            );
            log.info("AM citit din local " + "pictures/desk" + rand + ".jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void scrieHttpResponseInTmp(Object[] imgs, final File[] tmp){
        if(imgs[0] instanceof HttpResponse) {
            HttpResponse res = (HttpResponse) imgs[0];
            FileUtil.writeBytesToFile(res.bodyBytes(), tmp[0]);
        } else {
            log.info("NU AM CE FACE CU imgs[0] = " + imgs[0]);
        }
    }

    public static void setDesktopImage(String desktopImage) {
        final Object imgs[] = new Object[]{getInstance().find(desktopImage)};



        if(null == imgs[0]) {
            log.info("Search some default");
            findSomeDefault(imgs);
        }



        File out = new File(FileUtil.findPicturesDir(), "arise-desktop.png");
        final File[] tmp = {FileUtil.findSomeTempFile("tmp_desk")};
        if(tmp[0].exists()){
            tmp[0].delete();
        }


        scrieHttpResponseInTmp(imgs, tmp);
        

        if(imgs[0] instanceof String) {
            final String x = (String) imgs[0];
            if(x.startsWith("http")) {

                NetworkUtil.pingUrl(x, new Handler<URLConnection>() {
                    @Override
                    public void handle(URLConnection urlConnection) {
                        FileUtil.findSomeTempFile("tmp_desk").delete();
                        Object p = CommandRegistry.getInstance().getCommand("process-exec")
                                .execute("curl", x, "-o", FileUtil.findSomeTempFile("tmp_desk").getAbsolutePath());
                        if(p instanceof Process){
                            try {
                                ((Process)p).waitFor();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Handler<Object>() {
                    @Override
                    public void handle(Object o) {
                        FileUtil.findSomeTempFile("tmp_desk").delete();
                        findSomeDefault(imgs);
                    }
                });
            }
        }

        if(!FileUtil.findSomeTempFile("tmp_desk").exists()){
            scrieHttpResponseInTmp(imgs, tmp);
        }

        tmp[0] = FileUtil.findSomeTempFile("tmp_desk");
        if(tmp[0].exists() && CommandRegistry.getInstance().containsCommand("set-desktop-background")) {
            CommandRegistry.getInstance().execute("set-desktop-background", new String[]{tmp[0].getAbsolutePath(), out.getAbsolutePath(), desktopImage });
        } else {
			System.out.println("NU EXISTA TMP-UL");
		}

    }


    public static void findSomeDefault(final Object imgs[]){
        if(urlIndex[0] > urls.size() - 1) {
            urlIndex[0] = 0;
        }
        final String nextUrl= urls.get(urlIndex[0]);

        log.info("Cautam default");
        NetworkUtil.pingUrl(nextUrl, new Handler<URLConnection>() {
            @Override
            public void handle(URLConnection httpURLConnection) {
                imgs[0]= nextUrl;
                urlIndex[0]++;
                log.info("E ok " + imgs[0]);
            }
        }, new Handler<Object>() {
            @Override
            public void handle(Object err) {
                log.info("Nu e ok " + nextUrl);
                urlIndex[0]++;
                if(urlIndex[0] > urls.size() - 1) {
                    urlIndex[0] = 0;
                    log.info("Am terminat de iterat ");
                    citesteDefaultDinLocal(imgs);
                } else {
                    findSomeDefault(imgs);
                }
            }
        });
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
            String path = randomPickElement(root.get(id));
            return decodePath(path);

        }
        //idul devine query
        String query = id;
        for (String key: root.keySet()){
            if (query.toLowerCase().indexOf(key.toLowerCase()) > -1){
                return decodePath(randomPickElement(root.get(key)));
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
