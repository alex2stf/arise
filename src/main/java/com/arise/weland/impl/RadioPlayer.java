package com.arise.weland.impl;


import com.arise.astox.net.models.Peer;
import com.arise.canter.CommandRegistry;
import com.arise.canter.Cronus;
import com.arise.cargo.management.DependencyManager;
import com.arise.core.exceptions.LogicalException;
import com.arise.core.models.Handler;
import com.arise.core.models.Tuple2;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.ThreadUtil;
import com.arise.core.tools.Util;
import com.arise.weland.model.MediaPlayer;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.*;

import static com.arise.canter.Cronus.*;
import static com.arise.core.serializers.parser.Groot.decodeBytes;
import static com.arise.core.serializers.parser.Groot.decodeFile;
import static com.arise.core.tools.CollectionUtil.*;
import static com.arise.core.tools.FileUtil.findStream;
import static com.arise.core.tools.FileUtil.getRandomFileFromDirectory;
import static com.arise.core.tools.MapUtil.*;
import static com.arise.core.tools.StreamUtil.toBytes;
import static com.arise.core.tools.ThreadUtil.*;
import static com.arise.core.tools.Util.EXTFMT;
import static com.arise.core.tools.Util.nowCalendar;


public class RadioPlayer {

    List<Show> shows = new ArrayList<>();

    private static final CommandRegistry cmdReg = DependencyManager.getCommandRegistry();

    static MediaPlayer mPlayer = null;

    private volatile boolean is_play = false;


    private volatile String _cpath = "";

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

    public RadioPlayer onStreamChanged(Handler<RadioPlayer.Show> sc) {
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

    public void stop() {
        if (!is_play) {
            return;
        }
        if (st != null) {
            st.handle(this);
        }
        if (c != null) {
            c.stop();
        }

        mPlayer.stop(new Handler<MediaPlayer>() {
            @Override
            public void handle(MediaPlayer mediaPlayer) {
                is_play = false;
                _cpath = "";
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
            log.warn("no valid show defined for now... retry in " + lR + "ms " + EXTFMT.format(Util.now()));
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

    public static class Show {
        int _t;
        String _d;
        String _h;
        String _f;
        String _v;
        List<String> _s;
        String _m;
        String n;
        volatile boolean _o;
        RadioPlayer p;
        ThreadUtil.TimerResult t;
        Handler<Show> _osc;

        public String name() {
            return n;
        }

        public boolean isActive() {
            boolean act = (Cronus.matchMoment(nowCalendar(), _d, _h));
            if (act) {
                log.info("Show [" + n + "] active = " + act);
            }

            return act;
        }

        void scp(String x) {
            if (p != null) {
                p._cpath = x;
            }
        }


        public synchronized void run(final Handler<Show> c) {

            if (StringUtil.hasText(_v)) {
                if (_v.endsWith("%") && StringUtil.hasText(mPlayer.getMaxVolume())) {
                    String num = _v.substring(0, _v.length() - 1);
                    try {
                        Integer max = Integer.parseInt(mPlayer.getMaxVolume());
                        Integer percent = Integer.parseInt(num);
                        mPlayer.setVolume((max / (100 / percent) + ""));
                    } catch (Exception e) {
                        log.warn("Unable to set volume because", e);
                    }
                }
            }
            play_from_list_of_strings(c, _s, 0);
        }



        void setup_stream_close(final Handler<Show> c, long ex) {
            t = delayedTask(new Runnable() {
                @Override
                public void run() {
                    _o = false;
                    mPlayer.stop(new Handler<MediaPlayer>() {
                        @Override
                        public void handle(MediaPlayer mediaPlayer) {
                            log.i("Show [" + n + "] stooped at " + Util.now());
                            trigger(c);
                        }
                    });

                }
            }, ex);
        }

        void close_all_resources() {
            if (_o) {
                closeTimer(t);
                _o = false;


                mPlayer.stop(new Handler<MediaPlayer>() {
                    @Override
                    public void handle(MediaPlayer mediaPlayer) {
                        closeTimer(t);
                        _o = false;
                        log.info("Closing show [" + n + "] resources");
                    }
                });

            }
        }

        void play_from_list_of_strings(final Handler<Show> c, final List<String> urls, final int retryIndex) {
            close_all_resources();
            Map<Integer, List<String>> parts = Cronus.getParts(_h);
            long exp = 4000;
            if (parts.containsKey(2)) {
                Cronus.MomentInDay m = fromString(parts.get(2).get(1));
                if (m != null) {
                    Calendar li = decorate(m, Util.nowCalendar());
                    exp = Math.abs(li.getTimeInMillis() - nowCalendar().getTimeInMillis());
                }
            }

            if (retryIndex > urls.size()) {
                log.error("Urls list iteration complete. Are you connected to the internet?");
                setup_stream_close(c, exp);
                return;
            }

            final String u;
            if (_m.indexOf("linear-pick") > -1) {
                u = pickFromList(urls, false);
            } else {
                u = randomPick(urls);
            }

            //daca e fisier local
            if (u.startsWith("file:")) {
                String path = u.substring("file:".length());
                File root = new File(path);
                if (!root.exists() && !root.isDirectory()) {
                    log.warn("Directory " + root.getAbsolutePath() + " does not exist");

                    if (StringUtil.hasText(_f)) {
                        log.info("Using fallback " + _f);
                        p.forceStartActiveShow(_f, c);
                    } else {
                        play_from_list_of_strings(c, urls, retryIndex + 1);
                    }
                    return;
                }
                File f = getRandomFileFromDirectory(root.getAbsolutePath());
                log.info("Play " + f.getAbsolutePath());
                if (f == null) {
                    if (StringUtil.hasText(_f)) {
                        log.info("Using fallback " + _f);
                        p.forceStartActiveShow(_f, c);
                    } else {
                        play_from_list_of_strings(c, urls, retryIndex + 1);
                    }
                    return;
                }
                scp(f.getAbsolutePath());
                final Show self = this;
                mPlayer.play(f.getAbsolutePath(), new Handler<String>() {
                    @Override
                    public void handle(String s) {
                        _osc.handle(self);
                        trigger(c);
                    }
                });
                return;
            }


            final long finalExp = exp;
            log.info("Show [" + n + "] should end in " + strfMillis(finalExp));

            //default consideram ca e URL
            mPlayer.validateStreamUrl(u, new Handler<HttpURLConnection>() {
                        @Override
                        public void handle(HttpURLConnection huc) {
                            log.info("Start stream show [" + n + "] with url " + u);
                            scp(u);
                            mPlayer.playStream(u);
                            setup_stream_close(c, finalExp);
                            _o = true;
                        }
                    }, new Handler<Tuple2<Throwable, Peer>>() {
                @Override
                public void handle(Tuple2<Throwable, Peer> errTpl) {
                    log.error("Check url " + u + " failed", errTpl.first());
                    close_all_resources();
                    play_from_list_of_strings(c, urls, retryIndex + 1);
                    _o = false;
                }
            }
            );


        }


        void trigger(Handler<Show> c) {
            if (_t > 999) {
                System.out.println("sleep for " + _t);
                try {
                    Thread.sleep(_t);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            c.handle(this);
        }


        public void stop() {

            closeTimer(t);
            mPlayer.stop(new Handler<MediaPlayer>() {
                @Override
                public void handle(MediaPlayer mediaPlayer) {

                }
            });
        }
    }

    private void forceStartActiveShow(String name, Handler<Show> c) {
        stop();
        for (Show s : shows) {
            if (name.equalsIgnoreCase(s.n)) {
                s.run(c);
                return;
            }
        }
        throw new LogicalException("At least one valid fallback should be defined");
    }


}
