package com.arise.weland.impl;


import com.arise.canter.CommandRegistry;
import com.arise.cargo.management.DependencyManager;
import com.arise.core.exceptions.LogicalException;
import com.arise.core.models.Handler;
import com.arise.core.tools.*;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.dto.Playlist;
import com.arise.weland.model.MediaPlayer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.arise.core.serializers.parser.Groot.decodeBytes;
import static com.arise.core.tools.CollectionUtil.isEmpty;
import static com.arise.core.tools.FileUtil.findStream;
import static com.arise.core.tools.MapUtil.*;
import static com.arise.core.tools.StreamUtil.toBytes;
import static com.arise.core.tools.ThreadUtil.sleep;
import static com.arise.core.tools.Util.EXTFMT;


public class RadioPlayer {

    List<Show> shows = new ArrayList<>();


    static MediaPlayer mPlayer = null;

    private volatile boolean is_play = false;


    volatile String _cpath = "";

    private static final Mole log = Mole.getInstance("RD-PLAYER");

    ContentInfoProvider contentInfoProvider;

    public static MediaPlayer getMediaPlayer() {
        if (mPlayer == null) {
            mPlayer = MediaPlayer.getMediaPlayer("radio");
        }
        return mPlayer;
    }


    public RadioPlayer() {
        getMediaPlayer();
    }

    private Handler<RadioPlayer> pl;
    private Handler<RadioPlayer> st;
    private Handler<Show> sc;

    private Show c;

    public boolean isPlaying() {
        return is_play;
    }

    public String getCurrentDisplayName() {
        File f = new File(_cpath);
        if (f.exists()) {
            return f.getName();
        }
        return _cpath;
    }

    public String getCurrentPath() {
        return _cpath;
    }

    public RadioPlayer onPlay(Handler<RadioPlayer> pl) {
        this.pl = pl;
        return this;
    }

    public RadioPlayer onStop(Handler<RadioPlayer> st) {
        this.st = st;
        return this;
    }

    public RadioPlayer onStreamChanged(Handler<Show> sc) {
        this.sc = sc;
        return this;
    }


    public void play() {
        if (is_play) {
            return;
        }
        is_play = true;
        if (pl != null) {
            pl.handle(this);
        }
        loop();
    }


    public void restart(){
        stop(new Handler<RadioPlayer>() {
            @Override
            public void handle(RadioPlayer r) {
                r.play();
            }
        });
    }

    public void stop() {
        stop(null);
    }

    public void stop(final Handler<RadioPlayer> h) {
        if (!is_play) {
            return;
        }
        if (c != null) {
            c.stop(new Handler<MediaPlayer>() {
                @Override
                public void handle(MediaPlayer mediaPlayer) {
                    stop_mplayer(h);
                }
            });
        } else {
            stop_mplayer(h);
        }
    }


    void stop_mplayer(final Handler<RadioPlayer> h){
        final RadioPlayer self = this;
        log.info("Radio calls stop");
        mPlayer.stop(new Handler<MediaPlayer>() {
            @Override
            public void handle(MediaPlayer mediaPlayer) {
                is_play = false;
                _cpath = "";
                if(h != null) {
                    h.handle(self);
                }
                if (st != null) {
                    st.handle(self);
                }
            }
        });
    }

    int lR = 1000;

    private void loop() {
        AppDispatcher.tick();
        if (MediaPlayer.isAppClosed) {
            log.warn("Media player closed, loop disabled");
            return;
        }
        Show s = getActiveShow();
        if (s == null) {
            log.warn("no valid show defined for now... retry in " + lR + "ms at " + EXTFMT.format(Util.now()));
            sleep(lR);
            //limit to 20 minutes
            if (lR < 1000 * 60 * 20) {
                lR += 500;
            }
            if (is_play || !MediaPlayer.isAppClosed) {
                loop();
            }
            return;
        }
        lR = 1000;
        s.p = this;
        s.run(new Handler<Show>() {
            @Override
            public void handle(Show show) {
                if (is_play || !MediaPlayer.isAppClosed) {
                    log.info("Entering loop");
                    loop();
                }
            }
        });
    }

    public Show getActiveShow() {
        for (Show s : shows) {
            if (s.isActive()) {
                c = s;
                return s;
            }
        }
        return null;
    }


    public void addShow(Show s) {
        shows.add(s);
    }

    public void loadShowsResourcePath(String p) {
        try {


            Map m = (Map) decodeBytes(toBytes(findStream(p)));
            Map<Object, Object> lx = getMap(m, "lists");
            Map<String, List<String>> lists = new HashMap<>();

            if (!isEmpty(lx)) {
                for (Map.Entry<Object, Object> e : lx.entrySet()) {
                    if (e.getValue() instanceof List) {
                        Set<String> c = new HashSet<>();
                        for (Object zz : ((List) e.getValue())) {
                            if (zz instanceof String) {
                                c.add(zz + "");
                            } else if (zz instanceof Map) {
                                Map zm = (Map) zz;
                                String zmp = MapUtil.getString(zm, "path");
                                if(StringUtil.hasText(zmp)){
                                    c.add(zmp);
                                    if(null != contentInfoProvider){
                                        contentInfoProvider.mergeContent(
                                                contentInfoProvider.fromMap(zm), Playlist.STREAMS
                                        );
                                    }
                                }
                            }
                        }
                        String k = e.getKey() + "";
                        if (!lists.containsKey(k)) {
                            lists.put(k, new ArrayList<String>(c));
                        }
                    }
                }
            }

            List<Map> x = getList(m, "shows");
            for (Map h : x) {
                Show s = new Show();
                s.n = getString(h, "name");
                s._h = getString(h, "hour");
                s._d = getString(h, "day");
                List list = getList(h, "sources");
                s._LiD = s.n; //UUID.nameUUIDFromBytes(StringUtil.join(list, "_").getBytes()).toString();
                s._s = merge(list, lists);
                s._m = getString(h, "strategy");
                s._f = getString(h, "fallback");
                s._v = getString(h, "volume");
                s._t = getInt(h, "delay", 0);
                s._osc = new Handler<Show>() {
                    @Override
                    public void handle(Show show) {
                        sc.handle(show);
                    }
                };
                shows.add(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private List<String> merge(List<Object> src, Map<String, List<String>> buf) {
        if (isEmpty(buf)) {
            return new ArrayList<>();
        }

        List<String> res = new ArrayList<>();

        for (Object o : src) {
            if(o instanceof String){
                String s = (String) o;
                if (s.startsWith("${") && s.endsWith("}")) {
                    String key = s.substring(2, s.length() - 1);
                    for (String u : buf.get(key)) {
                        addValidString(res, u);
                    }
                } else {
                    addValidString(res, s);
                }
            }

            if(o instanceof Map){
                Map m = (Map) o;
                String path = MapUtil.getString(m, "path");
                addValidString(res, path);
                if(contentInfoProvider != null){
                    ContentInfo c = contentInfoProvider.fromMap(m);
                    contentInfoProvider.mergeContent(c, Playlist.STREAMS);
                }

            }

        }

        return res;
    }



    void forceStartActiveShow(String name, Handler<Show> c) {
        stop();
        for (Show s : shows) {
            if (name.equalsIgnoreCase(s.n)) {
                s.run(c);
                return;
            }
        }
        throw new LogicalException("At least one valid fallback should be defined");
    }

    static String smartPick(List<String> urls, boolean streamsFirst, String name){
        List<String> strms = new ArrayList<>();
        List<String> lcls = new ArrayList<>();

        for (String s: urls){
            if(s.startsWith("file:")) {
                lcls.add(s);
            } else {
                strms.add(s);
            }
        }

        List<String> lstr;
        if(streamsFirst) {
            lstr = CollectionUtil.concat(strms, lcls);
        } else {
            lstr = CollectionUtil.concat(lcls, strms);
        }
        return CollectionUtil.pickFromPersistentList(lstr, false, name);
    }

    public void setContentInfoProvider(ContentInfoProvider contentInfoProvider) {
        this.contentInfoProvider = contentInfoProvider;
    }
}
