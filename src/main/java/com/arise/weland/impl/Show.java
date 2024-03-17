package com.arise.weland.impl;

import com.arise.astox.net.models.Peer;
import com.arise.canter.Cronus;
import com.arise.core.models.Handler;
import com.arise.core.models.Tuple2;
import com.arise.core.tools.*;
import com.arise.weland.model.MediaPlayer;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static com.arise.canter.Cronus.*;
import static com.arise.core.tools.CollectionUtil.pickFromList;
import static com.arise.core.tools.CollectionUtil.randomPick;
import static com.arise.core.tools.FileUtil.getRandomFileFromDirectory;
import static com.arise.core.tools.StringUtil.hasContent;
import static com.arise.core.tools.ThreadUtil.closeTimer;
import static com.arise.core.tools.ThreadUtil.delayedTask;
import static com.arise.core.tools.Util.nowCalendar;
import static com.arise.weland.impl.RadioPlayer.smartPick;
import static java.lang.System.getProperty;

/**
 * Created by alex2 on 16/03/2024.
 */
public class Show {
    int _t;
    String _d;
    String _h;
    String _f;
    String _v;
    List<String> _s;
    String _m;
    String n;
    volatile boolean _up;
    RadioPlayer p;
    ThreadUtil.TimerResult t;
    Handler<Show> _osc;

    private static final MediaPlayer mPlayer = RadioPlayer.getMediaPlayer();

    private static final Mole log = Mole.getInstance(Show.class);

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

        _up = true;
        play_from_list_of_strings(c, _s, 0);

    }



    void setup_stream_close(final Handler<Show> c, long ex) {
        t = delayedTask(new Runnable() {
            @Override
            public void run() {
                _up = false;
                log.info("setup_stream_close media.stop()");
                mPlayer.stop(new Handler<MediaPlayer>() {
                    @Override
                    public void handle(MediaPlayer mediaPlayer) {
                        log.i("Show [" + n + "] stooped at " + Util.now());
                        c.handle(null);
                    }
                });

            }
        }, ex);
    }

    void close_all_resources(final Handler<Object> onComplete) {
        if (_up) {
            closeTimer(t);
            _up = false;

            log.info("close_all_resources media.stop()");
            mPlayer.stop(new Handler<MediaPlayer>() {
                @Override
                public void handle(MediaPlayer mediaPlayer) {
                    closeTimer(t);
                    _up = false;
                    log.info("Show [" + n + "] resources closed");
                    onComplete.handle(this);
                }
            });
        } else {
            log.info("Nothing to close");
            onComplete.handle(this);
        }
    }

    void continue_pick(final Handler<Show> c, final List<String> urls, final int retryIndex){
        log.info("continue_pick " + retryIndex);
        _up = false;
        clear_sys_props();
        if (StringUtil.hasText(_f)) {
            log.info("Using fallback " + _f);
            p.forceStartActiveShow(_f, c);
        } else {
            play_from_list_of_strings(c, urls, retryIndex);
        }
    }

    void clear_sys_props(){
        System.clearProperty("radio.forced.path");
    }

    void handle_local_finished(final Handler<Show> c){

        if(_up) {
            clear_sys_props();
            _osc.handle(this);
            c.handle(this);
            _up = false;
        } else {
            log.info("show is not _up");
        }
    }

    void play_from_list_of_strings(final Handler<Show> c, final List<String> urls, final int retryIndex) {
        close_all_resources(new Handler<Object>() {
            @Override
            public void handle(Object o) {
                //init functie
                log.info("Entring play_from_list_of_strings iteration " + retryIndex);

                Map<Integer, List<String>> parts = Cronus.getParts(_h);
                long exp = 4000;
                if (parts.containsKey(2)) {
                    Cronus.MomentInDay m = fromString(parts.get(2).get(1));
                    if (m != null) {
                        Calendar li = decorate(m, Util.nowCalendar());
                        exp = Math.abs(li.getTimeInMillis() - nowCalendar().getTimeInMillis());
                    }
                }

                if (retryIndex > urls.size() + 1) {
                    log.error("Urls list iteration complete. Are you connected to the internet?");
                    setup_stream_close(c, exp);
                    return;
                }

                final String pdir;

                if(hasContent(getProperty("radio.forced.path"))) {
                    pdir = getProperty("radio.forced.path");
                    clear_sys_props();
                }
                else if (_m.toLowerCase().indexOf("linear-pick") > -1) {
                    pdir = pickFromList(urls, false);
                }
                else if(_m.toLowerCase().indexOf("stream-first") > -1) {
                    pdir = smartPick(urls, true);
                }
                else if(_m.toLowerCase().indexOf("local-first") > -1) {
                    pdir = smartPick(urls, false);
                }
                else {
                    pdir = randomPick(urls);
                }

                //daca e fisier local
                if(ContentType.isMedia(pdir) && new File(pdir).exists()) {
                    File pflf = new File(pdir);
                    scp(pflf.getAbsolutePath());
                    _up = true;
                    mPlayer.play(pflf.getAbsolutePath(), new Handler<String>() {
                        @Override
                        public void handle(String s) {
                            handle_local_finished(c);
                        }
                    });
                    return;
                }

                //daca e format file:
                else if (pdir.startsWith("file:")) {
                    String path = pdir.substring("file:".length());
                    File file = new File(apply_variables(path));
                    if (!file.exists()) {
                        log.warn("File " + file.getAbsolutePath() + " does not exist");
                        continue_pick(c, urls, retryIndex + 1);
                        return;
                    }

                    File pfl;
                    if(ContentType.isMedia(file)) {
                        pfl = file.getAbsoluteFile();
                    } else {
                        pfl = getRandomFileFromDirectory(file.getAbsolutePath());
                    }


                    if (pfl == null) {
                        log.info("NULL AT: " + pfl.getAbsolutePath());
                        continue_pick(c, urls, retryIndex + 1);
                        return;
                    }
                    log.info("Play " + pfl.getAbsolutePath());
                    scp(pfl.getAbsolutePath());
                   _up = true;
                    mPlayer.play(pfl.getAbsolutePath(), new Handler<String>() {
                        @Override
                        public void handle(String s) {
                            log.info("Random pick finihed play, _up=" + _up);
                            handle_local_finished(c);
                        }
                    });
                    return;
                } else if(ContentType.isHttpPath(pdir)) {
                    final long finalExp = exp;
                    log.info("Show [" + n + "] should end in " + strfMillis(finalExp));
                    _up = false;

                    //default consideram ca e URL
                    mPlayer.validateStreamUrl(pdir, new Handler<HttpURLConnection>() {
                        @Override
                        public void handle(HttpURLConnection huc) {
                            log.info("Start stream show [" + n + "] with url " + pdir);
                            clear_sys_props();
                            scp(pdir);
                            mPlayer.playStream(pdir);
                            setup_stream_close(c, finalExp);
                            _up = true;
                        }
                    }, new Handler<Tuple2<Throwable, Peer>>() {
                        @Override
                        public void handle(Tuple2<Throwable, Peer> errTpl) {
                            log.error(retryIndex + ") iteration check url " + pdir + " failed");
                            clear_sys_props();
                            close_all_resources(new Handler<Object>() {
                                @Override
                                public void handle(Object o) {
                                    play_from_list_of_strings(c, urls, retryIndex + 1);
                                    _up = false;
                                }
                            });

                        }
                    });
                } else {
                    log.info("WTF faci cu " + pdir + "??????");
                }

                //final de functie
            }
        });


    }

    private String apply_variables(String path) {
        if(path.indexOf("${music_dir}") > -1) {
            path = path.replace("${music_dir}", FileUtil.findMusicDir().getAbsolutePath());
        }

        if(path.indexOf("${usb_drive_0}") > -1 && hasContent(getProperty("usb.drive.0"))) {
            path = path.replace("${usb_drive_0}", getProperty("usb.drive.0"));
        }
        return path;
    }



    public void stop(Handler<MediaPlayer> h) {

        log.info("show stop");
        _up = false;
        closeTimer(t);
        mPlayer.stop(h);
    }
}