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
    private SGCache cacheStrategy;

    private static final Mole log = Mole.getInstance("SGSERVICE");

    private static final List<String> urls = new ArrayList<>();
    private static final int [] urlIndex = new int[]{0};
    static {
        urls.add("https://i.pinimg.com/originals/43/8f/a8/438fa8f38d01e429201126e13c8015df.jpg");
        urls.add("https://wmpskinsarchive.neocities.org/images/Batman%20Begins.png");
        urls.add("https://images.pexels.com/photos/1010519/pexels-photo-1010519.jpeg");
        urls.add("https://wallsdesk.com/wp-content/uploads/2016/07/Scarlett-Johansson-Sexy-Wallpapers.jpg");
        urls.add("https://i.pinimg.com/originals/9b/fd/a0/9bfda0efb535e51570c6648a41e7a3c8.jpg");
        urls.add("https://getwallpapers.com/wallpaper/full/0/2/0/1410169-most-popular-blues-music-wallpaper-1920x1200-for-meizu.jpg");
        urls.add("https://mcdn.wallpapersafari.com/medium/35/85/WFlGVS.jpg");
        urls.add("https://www.desktopbackground.org/p/2015/09/29/1018545_download-10-stunning-sexy-ubuntu-wallpapers_1920x1200_h.jpg");
        urls.add("https://www.free-codecs.com/pictures/screenshots/bsplayer.jpg");
        urls.add("https://media-assets.wired.it/photos/615f319b5b820b8db3e7cd47/master/w_1600%2Cc_limit/1518175196_Winamp.jpg");
        urls.add("https://media.askvg.com/articles/images5/Winamp_Skin_VLC_Media_Player.png");
        urls.add("https://wmpskinsarchive.neocities.org/images/tubeframe.png");
        urls.add("https://cafans.b-cdn.net/images/Category_50149/subcat_125029/Woman_of_X-men_U2.jpg");
        urls.add("https://img.goodfon.com/original/1920x1200/c/ab/devushka-art-by-dandonfuga-marvel-rouge-x-man.jpg");
        urls.add("https://images5.alphacoders.com/488/488512.jpg");
        urls.add("https://pbs.twimg.com/media/C2SRGUIXAAArnz6.jpg");
        urls.add("https://cdn.wallpapersafari.com/25/2/O9viq2.jpg");
        urls.add("https://w0.peakpx.com/wallpaper/86/103/HD-wallpaper-halle-berry-berry-halle-actress-sexy.jpg");
        urls.add("https://wmpskinsarchive.neocities.org/images/STALKER.png");
        urls.add("https://w0.peakpx.com/wallpaper/13/473/HD-wallpaper-scarlett-johansson-beautiful-sexy-02.jpg");
        urls.add("https://e0.pxfuel.com/wallpapers/150/412/desktop-wallpaper-sexy-in-black-sexy-lingerie-model-woman.jpg");
        urls.add("https://wmpskinsarchive.neocities.org/images/QuickSilver.png");
        urls.add("https://images.unsplash.com/photo-1511379938547-c1f69419868d");
        urls.add("https://wmpskinsarchive.neocities.org/images/Main_Street.png");
        urls.add("https://wmpskinsarchive.neocities.org/images/Plus!%20Space.png");
        urls.add("https://images.sftcdn.net/images/t_app-cover-l,f_auto/p/a87cbe22-96da-11e6-a0e8-00163ed833e7/1782960444/vlc-media-player-skins-pack-screenshot.jpg");
        urls.add("https://wmpskinsarchive.neocities.org/images/WWN_mp7.png");
        urls.add("https://wmpskinsarchive.neocities.org/images/Plus!%20SlimLine.png");
        urls.add("https://wmpskinsarchive.neocities.org/images/Windows_XP_Media_Center_Edition.png");
        urls.add("https://m.media-amazon.com/images/I/71FydjvsrcL._AC_SX679_.jpg");
        urls.add("https://getwallpapers.com/wallpaper/full/d/c/8/1410156-widescreen-blues-music-wallpaper-1920x1200-samsung-galaxy.jpg");
        urls.add("https://wmpskinsarchive.neocities.org/images/digitaldj.png");
        urls.add("https://w0.peakpx.com/wallpaper/495/184/HD-wallpaper-sexy-female-art-art-female-model-abstract-sexy.jpg");
        urls.add("https://wmpskinsarchive.neocities.org/images/cerulean.png");
        urls.add("https://w.forfun.com/fetch/f2/f219d1569a3140e08415042a9112ff99.jpeg");
        urls.add("https://wmpskinsarchive.neocities.org/images/9SeriesDefault.png");
        urls.add("https://w.wallhaven.cc/full/42/wallhaven-42k88m.jpg");
        urls.add("https://wmpskinsarchive.neocities.org/images/ALXVortex.png");
        urls.add("https://live.staticflickr.com/65535/52607511849_af34b6e5d9.jpg");
        urls.add("https://mcdn.wallpapersafari.com/medium/40/55/gtBrSV.jpg");
        urls.add("https://i.ytimg.com/vi/bhc7y-n7vIU/hqdefault.jpg");
        urls.add("https://wmpskinsarchive.neocities.org/images/Erektorset.png");
        urls.add("https://wmpskinsarchive.neocities.org/images/Gold.png");
        urls.add("https://wmpskinsarchive.neocities.org/images/Goo.png");
        urls.add("https://images.unsplash.com/photo-1557244056-ac3033d17d9a");
        urls.add("https://getwallpapers.com/wallpaper/full/4/1/5/1410163-large-blues-music-wallpaper-1920x1200.jpg");
        urls.add("https://getwallpapers.com/wallpaper/full/e/6/0/1410148-download-blues-music-wallpaper-1920x1080-ipad-pro.jpg");
        urls.add("https://getwallpapers.com/wallpaper/full/9/d/4/1410145-blues-music-wallpaper-1920x1080-windows-7.jpg");
        urls.add("https://getwallpapers.com/wallpaper/full/f/c/b/1410170-full-size-blues-music-wallpaper-2560x1600-ios.jpg");
        urls.add("https://images.fineartamerica.com/images/artworkimages/mediumlarge/1/in-the-end-swedish-attitude-design.jpg");
        urls.add("https://1.bp.blogspot.com/_SWYwL3fIkFs/S95uhByssMI/AAAAAAAAEp8/FXftVrz7Ii4/s1600/oil+painting+abstract+windows+media+player+skin.png");
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

//        NetworkUtil.pingUrl(x, new Handler<URLConnection>() {
//            @Override
//            public void handle(URLConnection urlConnection) {
//                FileUtil.findSomeTempFile("tmp_desk").delete();
//                Object p = CommandRegistry.getInstance().getCommand("process-exec")
//                        .execute("curl", x, "-o", FileUtil.findSomeTempFile("tmp_desk").getAbsolutePath());
//                if(p instanceof Process){
//                    try {
//                        ((Process)p).waitFor();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }, onErr);
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
            CommandRegistry.getInstance().execute("set-desktop-background", new String[]{tmpDesk().getAbsolutePath(), out.getAbsolutePath(), desktopImage });
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
