package com.arise.weland.impl;


import com.arise.canter.CommandRegistry;
import com.arise.canter.Defaults;
import com.arise.core.tools.AppCache;
import com.arise.core.tools.Mole;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.arise.core.serializers.parser.Groot.decodeBytes;
import static com.arise.core.tools.FileUtil.findStream;
import static com.arise.core.tools.MapUtil.getList;
import static com.arise.core.tools.MapUtil.getString;
import static com.arise.core.tools.StreamUtil.toBytes;
import static java.util.Collections.shuffle;

public class RadioPlayer {

    List<Show> shows = new ArrayList<>();
    MediaPlayer mPlayer;

    private volatile boolean is_play = true;

    private static final Mole log = Mole.getInstance(RadioPlayer.class);


    public RadioPlayer(CommandRegistry cmdReg){
        mPlayer = MediaPlayer.getInstance("radio", cmdReg);
    }



    public void play() {
        is_play = true;
        loop();
    }

    private void loop(){
        for (Show s: shows){
            if (s.isActive()){
                s.start(mPlayer);
            }
            break;
        }
        if (is_play) {
            loop();
        }
    }

    public void loadShowsResourcePath(String p) {
        try {
            Map m = (Map) decodeBytes(toBytes(findStream(p)));
            System.out.println(m);

            List<Map> x = getList(m, "shows");
            for (Map h: x){
                Show s = new Show();
                s.n = getString(h, "name");
                s._h = getString(h, "hour");
                s._d = getString(h, "day");
                s._s = getList(h, "sources");
                s._m = getString(h, "strategy");
                shows.add(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static class Show {
        String _d;
        String _h;
        List<String> _s;
        String _m;
        String n;


        public Show name(String x){
            this.n = x;
            return this;
        }

        public String name(){
            return n;
        }

        public boolean isActive() {
            return true;
        }

        public void start(MediaPlayer mPlayer) {
            String p = _s.get(0);
            File f = getRandomFileFromDirectory(p);
            mPlayer.play(f.getAbsolutePath());
        }


    }


    public static final File getRandomFileFromDirectory(String path){

        if (Defaults.closed == true){
            return null;
        }
        if (!new File(path).exists()){
            log.w("Path " + path + " does not exist");
        } else {
            log.trace("select random file from " + path);
        }

        String listId = "rand-file-" + UUID.nameUUIDFromBytes(path.getBytes());
        AppCache.StoredList storedList = AppCache.getStoredList(listId);
        if (storedList.isEmpty() || storedList.isIndexExceeded()){

            File dir = new File(path);
            File[] files = dir.listFiles();
            if (files == null || files.length == 0){
                return null;
            }
            List<String> items = new ArrayList<>();
            for (File f: files){
                items.add(f.getAbsolutePath());
            }
            shuffle(items);

            storedList = AppCache.storeList(listId, items, 0);
            log.info("reshuffled list " + listId);
        }

        List<String> saved = storedList.getItems();
        int index = storedList.getIndex();
        AppCache.storeList(listId, saved, index + 1);

        String selected = saved.get(index);

        File res = new File(selected);
        if (!res.exists()){
            log.warn("Path " + selected + "may not exist anymore");
        }

        return res;
    }
}
