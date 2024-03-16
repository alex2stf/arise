package com.arise.weland.impl;


import com.arise.astox.net.models.Peer;
import com.arise.canter.CommandRegistry;
import com.arise.canter.Cronus;
import com.arise.cargo.management.DependencyManager;
import com.arise.core.exceptions.LogicalException;
import com.arise.core.models.Handler;
import com.arise.core.models.Tuple2;
import com.arise.core.tools.*;
import com.arise.weland.model.MediaPlayer;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;

import static com.arise.canter.Cronus.*;
import static com.arise.core.serializers.parser.Groot.decodeBytes;
import static com.arise.core.tools.CollectionUtil.*;
import static com.arise.core.tools.FileUtil.findStream;
import static com.arise.core.tools.FileUtil.getRandomFileFromDirectory;
import static com.arise.core.tools.MapUtil.*;
import static com.arise.core.tools.StreamUtil.toBytes;
import static com.arise.core.tools.StringUtil.hasContent;
import static com.arise.core.tools.ThreadUtil.*;
import static com.arise.core.tools.Util.EXTFMT;
import static com.arise.core.tools.Util.nowCalendar;
import static java.lang.System.getProperty;


public class RadioPlayer {

    List<Show> shows = new ArrayList<>();

    private static final CommandRegistry cmdReg = DependencyManager.getCommandRegistry();

    static MediaPlayer mPlayer = null;

    private volatile boolean is_play = false;


    volatile String _cpath = "";

    private static final Mole log = Mole.getInstance(RadioPlayer.class);

    public static MediaPlayer getMediaPlayer() {
        if (mPlayer == null) {
            mPlayer = MediaPlayer.getMediaPlayer("radio", cmdReg);
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
        if (st != null) {
            st.handle(this);
        }
        if (c != null) {
            c.stop();
        }

        final RadioPlayer self = this;
        mPlayer.stop(new Handler<MediaPlayer>() {
            @Override
            public void handle(MediaPlayer mediaPlayer) {
            is_play = false;
            _cpath = "";
            if(h != null) {
                h.handle(self);
            }
            }
        });
    }

    int lR = 1000;

    private void loop() {
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
                        List<String> c = new ArrayList<>();
                        for (Object zz : ((List) e.getValue())) {
                            if (zz instanceof String) {
                                c.add(zz + "");
                            }
                        }
                        String k = e.getKey() + "";
                        if (!lists.containsKey(k)) {
                            lists.put(k, c);
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
                s._s = merge(getList(h, "sources"), lists);
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

    private List<String> merge(List<String> src, Map<String, List<String>> buf) {
        if (isEmpty(buf)) {
            return src;
        }

        List<String> res = new ArrayList<>();

        for (String s : src) {
            if (s.startsWith("${") && s.endsWith("}")) {
                String key = s.substring(2, s.length() - 1);
                for (String u : buf.get(key)) {
                    res.add(u);
                }
            } else {
                res.add(s);
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

    static String smartPick(List<String> urls, boolean streamsFirst){
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
        return CollectionUtil.pickFromList(lstr, false);
    }

}
