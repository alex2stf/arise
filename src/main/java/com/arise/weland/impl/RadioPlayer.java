package com.arise.weland.impl;


import com.arise.canter.CommandRegistry;
import com.arise.canter.Cronus;
import com.arise.cargo.management.DependencyManager;
import com.arise.core.models.Handler;
import com.arise.core.tools.Mole;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static com.arise.core.serializers.parser.Groot.decodeBytes;
import static com.arise.core.tools.FileUtil.findStream;
import static com.arise.core.tools.FileUtil.getRandomFileFromDirectory;
import static com.arise.core.tools.MapUtil.getInt;
import static com.arise.core.tools.MapUtil.getList;
import static com.arise.core.tools.MapUtil.getString;
import static com.arise.core.tools.StreamUtil.toBytes;
import static com.arise.core.tools.ThreadUtil.delayedTask;
import static com.arise.core.tools.ThreadUtil.sleep;

public class RadioPlayer {

    List<Show> shows = new ArrayList<>();

    private static final CommandRegistry cmdReg = DependencyManager.getCommandRegistry();

    static MediaPlayer mPlayer = MediaPlayer.getInstance("radio", cmdReg);

    private volatile boolean is_play = true;

    private static final Mole log = Mole.getInstance(RadioPlayer.class);

    public static MediaPlayer getMediaPlayer() {
        return mPlayer;
    }


    public void play() {
        is_play = true;
        loop();
    }

    int lR = 1000;
    private void loop(){
        if (MediaPlayer.isAppClosed){
            log.warn("Media player closed, loop disabled");
            return;
        }
        Show s = getActiveShow();
        if (s == null){
            log.warn("no valid show defined for now... retry in " + lR  + "ms");
            sleep(lR);
            lR += 100;
            if (is_play || !MediaPlayer.isAppClosed) {
                loop();
            }
            return;
        }
        lR = 1000;
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


        public Show name(String x){
            this.n = x;
            return this;
        }

        public String name(){
            return n;
        }

        public boolean isActive() {
            boolean act = (Cronus.matchMoment(Calendar.getInstance(), _d, _h));
            if (act){
                log.info("show " + n + " active = " + act);
            }
            return act;
        }


        public synchronized void run(Handler<Show> c){
            if ("media-play".equalsIgnoreCase(_m)){
                String p = _s.get(0);
                File f = getRandomFileFromDirectory(p);
                mPlayer.play(f.getAbsolutePath());
                trigger(c);
            }
            else if ("sound-over-music".equalsIgnoreCase(_m)){
                String s = _s.get(0);
                String m = _s.get(1);
                final File sf = getRandomFileFromDirectory(s);
                File mf = getRandomFileFromDirectory(m);

                long max = MediaPlayer.getAudioDurationMs(mf, 3000);
                final int time = (int) ((Math.random() * (max - 1000)) + 1000);

                delayedTask(new Runnable() {
                    @Override
                    public void run() {
                        log.info("snd " + sf.getAbsolutePath() + " delayed " + time);
                        MediaPlayer.getInstance("radio-sounds", cmdReg).play(sf.getAbsolutePath());
                    }
                }, time);
                mPlayer.play(mf.getAbsolutePath());
                trigger(c);
            }
            else if ("stream".equalsIgnoreCase(_m)){
                String u = _s.get(0);
                String p[] = Cronus.getParts(_h);
                long exp = 4000;
                if (p.length == 3){
                    Cronus.MomentInDay m = Cronus.fromString(p[2]);

                    if (m != null){
                        Calendar li = Cronus.decorate(m, Calendar.getInstance());
                        exp = Math.abs(li.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());

                        log.info("show " + n + " will end in " + (exp / 1000) + " seconds");
                    }
                }
                mPlayer.playStream(u);


                delayedTask(new Runnable() {
                    @Override
                    public void run() {
                        mPlayer.stop();
                    }
                }, exp);


            }

            else {
                System.out.println("wtf strategy is [" + _m + "] ?");
            }
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




    }



}
