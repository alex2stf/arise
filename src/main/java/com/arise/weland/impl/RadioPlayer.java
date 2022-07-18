package com.arise.weland.impl;


import com.arise.astox.net.models.Peer;
import com.arise.canter.CommandRegistry;
import com.arise.canter.Cronus;
import com.arise.cargo.management.DependencyManager;
import com.arise.core.exceptions.SyntaxException;
import com.arise.core.models.Handler;
import com.arise.core.models.Tuple2;
import com.arise.core.tools.CollectionUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.ThreadUtil;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.arise.canter.Cronus.decorate;
import static com.arise.canter.Cronus.fromString;
import static com.arise.canter.Cronus.strNow;
import static com.arise.canter.Cronus.strfMillis;
import static com.arise.canter.Cronus.strfNowPlusMillis;
import static com.arise.core.serializers.parser.Groot.decodeBytes;
import static com.arise.core.tools.CollectionUtil.randomPick;
import static com.arise.core.tools.CollectionUtil.removeFirst;
import static com.arise.core.tools.FileUtil.findStream;
import static com.arise.core.tools.FileUtil.getRandomFileFromDirectory;
import static com.arise.core.tools.MapUtil.*;
import static com.arise.core.tools.StreamUtil.toBytes;
import static com.arise.core.tools.ThreadUtil.*;
import static com.arise.core.tools.Util.randBetween;
import static java.util.Calendar.getInstance;

public class RadioPlayer {

    List<Show> shows = new ArrayList<>();

    private static final CommandRegistry cmdReg = DependencyManager.getCommandRegistry();

    static MediaPlayer mPlayer = MediaPlayer.getMediaPlayer("radio", cmdReg);

    private volatile boolean is_play = false;


    private volatile String _cpath = "";

    private static final Mole log = Mole.getInstance(RadioPlayer.class);

    public static MediaPlayer getMediaPlayer() {
        return mPlayer;
    }


    private Handler<RadioPlayer> pl;
    private Handler<RadioPlayer> st;

    private Show c;

    public boolean isPlaying(){
        return is_play;
    }

    public String getCurrentPath(){
        return _cpath;
    }

    public RadioPlayer onPlay(Handler<RadioPlayer> pl){
        this.pl = pl;
        return this;
    }

    public RadioPlayer onStop(Handler<RadioPlayer> st){
        this.st = st;
        return this;
    }


    public void play() {
        if (is_play){
            return;
        }
        is_play = true;
        if (pl != null){
            pl.handle(this);
        }
        loop();
    }

    public void stop(){
        if (!is_play){
            return;
        }
        if (st != null){
            st.handle(this);
        }
        if (c != null){
            c.stop();
        }

        mPlayer.stop();
        is_play = false;
        _cpath = "";
    }

    int lR = 1000;
    private void loop(){
        if (MediaPlayer.isAppClosed){
            log.warn("Media player closed, loop disabled");
            return;
        }
        Show s = getActiveShow();
        if (s == null){
            log.warn("no valid show defined for now... retry in " + lR  + "ms " + new Date());
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

    private Show getActiveShow(){
        for (Show s: shows){
            if (s.isActive()){
                c = s;
                return s;
            }
        }
        return null;
    }

    public void loadShowsResourcePath(String p) {
        try {
            Map m = (Map) decodeBytes(toBytes(findStream(p)));
            List<Map> x = getList(m, "shows");
            for (Map h: x){
                Show s = new Show();
                s.n = getString(h, "name");
                s._h = getString(h, "hour");
                s._d = getString(h, "day");
                s._s = getList(h, "sources");
                s._m = getString(h, "strategy");
                s._t = getInt(h, "delay", 0);
                shows.add(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static class Show {
        int _t;
        String _d;
        String _h;
        List<String> _s;
        String _m;
        String n;
        volatile boolean _o;
        RadioPlayer p;

        public Show name(String x){
            this.n = x;
            return this;
        }

        public String name(){
            return n;
        }

        public boolean isActive() {
            boolean act = (Cronus.matchMoment(getInstance(), _d, _h));
            if (act){
                log.info("show " + n + " active = " + act);
            }
            return act;
        }

        void scp(String x){
            if (p != null){
                p._cpath = x;
            }
        }


        public synchronized void run(final Handler<Show> c){
            if ("play-local".equalsIgnoreCase(_m)){
                String p = _s.get(0);
                File f = getRandomFileFromDirectory(p);
                scp(f.getAbsolutePath());
                mPlayer.play(f.getAbsolutePath(), new Handler<String>() {
                    @Override
                    public void handle(String s) {
                        trigger(c);
                    }
                });
            }
            else if ("sound-over-music".equalsIgnoreCase(_m)){
                String s = _s.get(0);
                String m = _s.get(1);
                final File sf = getRandomFileFromDirectory(s);
                File mf = getRandomFileFromDirectory(m);

                long max = MediaPlayer.getAudioDurationMs(mf, 3000);
                final int time = (int) ((Math.random() * (max - 1000)) + 1000);

                t = delayedTask(new Runnable() {
                    @Override
                    public void run() {
                        log.info("snd " + sf.getAbsolutePath() + " delayed " + time);
                        MediaPlayer.getMediaPlayer("radio-sounds", cmdReg).play(sf.getAbsolutePath());
                    }
                }, time);
                scp(mf.getAbsolutePath());
                mPlayer.play(mf.getAbsolutePath(), new Handler<String>() {
                    @Override
                    public void handle(String s) {
                        trigger(c);
                    }
                });


            }
            else if ("sound-over-stream".equalsIgnoreCase(_m)){
                pss(c, randomPick(removeFirst(1, _s)));
                psos(_s.get(0)); //sound
            }
            else if ("stream".equalsIgnoreCase(_m)){
                pss(c, randomPick(_s));
            }



            else {
                throw new SyntaxException("unknown strategy " + _m);
            }
        }

        ThreadUtil.TimerResult t;


        void psos(final String p){
            if (_o){
                closeTimer(t);
                int exp = randBetween(1000 * 60 * 5, 1000 * 60 * 20);
                log.info("sndPlay scheduled at " + strfNowPlusMillis(exp));
                t = delayedTask(new Runnable() {
                    @Override
                    public void run() {
                        if (_o) {
                            File f = getRandomFileFromDirectory(p);
                            try {
                                log.info("sndPlay " + f.getAbsolutePath() + " at " + strNow());
                                MediaPlayer.getMediaPlayer("radio-sounds", cmdReg).play(f.getAbsolutePath());
                                psos(p);
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                }, exp);
            }
        }


        void pss(final Handler<Show> c, final String u){
            _o = true;
            log.info("Start stream show [" + n + "] with url " + u);
            String p[] = Cronus.getParts(_h);
            long exp = 4000;
            if (p.length == 3){
                Cronus.MomentInDay m = fromString(p[2]);
                if (m != null){
                    Calendar li = decorate(m, getInstance());
                    exp = Math.abs(li.getTimeInMillis() - getInstance().getTimeInMillis());
                    log.info("show " + n + " will end in " + strfMillis(exp) );
                }
            }

            mPlayer.validateStreamUrl(u, new Handler<HttpURLConnection>() {
                @Override
                public void handle(HttpURLConnection httpURLConnection) {
                    scp(u);
                    mPlayer.playStream(u);
                }
            }, new Handler<Tuple2<Throwable, Peer>>() {
                @Override
                public void handle(Tuple2<Throwable, Peer> t) {
                    t.first().printStackTrace();
                }
            });



            t = delayedTask(new Runnable() {
                @Override
                public void run() {
                    _o = false;
                    mPlayer.stop();
                    log.i("show "+ n + " stooped at " + new Date());
                    trigger(c);
                }
            }, exp);
        }



        void trigger(Handler<Show> c){
            if (_t > 999) {
                System.out.println("sleep for "+ _t);
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
            MediaPlayer.getMediaPlayer("radio-sounds", cmdReg).stop();
            mPlayer.stop();
        }
    }



}
