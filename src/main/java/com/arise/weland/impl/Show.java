package com.arise.weland.impl;

import com.arise.canter.CommandRegistry;
import com.arise.canter.Cronus;
import com.arise.core.models.Handler;
import com.arise.core.tools.*;
import com.arise.weland.model.MediaPlayer;

import java.io.File;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static com.arise.canter.Cronus.*;
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
    String _LiD;
    String _m;
    String n;
    volatile boolean _playing;
    RadioPlayer p;
    ThreadUtil.TimerResult t;
    Handler<Show> _osc;

    private static final MediaPlayer mPlayer = RadioPlayer.getMediaPlayer();

    private static final Mole log = Mole.getInstance("RADIOSHOW");


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

    void set_current_path(String x) {
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

        _playing = true;
        play_from_list_of_strings(c, _s, 0);

    }



    void setup_stream_close(final Handler<Show> c, long ex) {
        t = delayedTask(new Runnable() {
            @Override
            public void run() {
                _playing = false;
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
        if (_playing) {
            closeTimer(t);
            _playing = false;

            mPlayer.stop(new Handler<MediaPlayer>() {
                @Override
                public void handle(MediaPlayer mediaPlayer) {
                    closeTimer(t);
                    _playing = false;
                    log.info(n + "] resources closed");
                    onComplete.handle(this);
                }
            });
        } else {
            onComplete.handle(this);
        }
    }

    void continue_pick(final Handler<Show> c, final List<String> urls, final int retryIndex){
        _playing = false;
        clear_sys_props();
        if (StringUtil.hasText(_f)) {
            log.info(n + "] using fallback " + _f);
            p.forceStartActiveShow(_f, c);
        } else {
            play_from_list_of_strings(c, urls, retryIndex);
        }
    }

    void clear_sys_props(){
        System.clearProperty("radio.forced.path");
    }

    void handle_local_finished(final Handler<Show> c){

        if(_playing) {
            clear_sys_props();
            _osc.handle(this);

            _playing = false;
        } else {
            log.info("show is not _playing");
        }

        c.handle(this);
    }

    void play_from_list_of_strings(final Handler<Show> c, final List<String> sources, final int retryIndex) {
        close_all_resources(new Handler<Object>() {
            @Override
            public void handle(Object o) {
                //init functie
                log.info(n + "] play_from_list_of_strings iteration " + retryIndex);

                Map<Integer, List<String>> parts = Cronus.getParts(_h);
                long exp = 4000;
                if (parts.containsKey(2)) {
                    Cronus.MomentInDay m = fromString(parts.get(2).get(1));
                    if (m != null) {
                        Calendar li = decorate(m, Util.nowCalendar());
                        exp = Math.abs(li.getTimeInMillis() - nowCalendar().getTimeInMillis());
                    }
                }

                if (retryIndex > sources.size() + 1) {
                    log.error(n + "] urls list iteration complete. Are you connected to the internet?");
                    setup_stream_close(c, exp);
                    return;
                }

                final String pdir;

                if(hasContent(getProperty("radio.forced.path"))) {
                    pdir = getProperty("radio.forced.path");
                    clear_sys_props();
                }
                else if (_m.toLowerCase().indexOf("linear-pick") > -1) {
                    pdir = CollectionUtil.pickFromPersistentList(sources, false, _LiD);
                }
                else if(_m.toLowerCase().indexOf("stream-first") > -1) {
                    pdir = smartPick(sources, true, _LiD);
                }
                else if(_m.toLowerCase().indexOf("local-first") > -1) {
                    pdir = smartPick(sources, false, _LiD);
                }
                else {
                    pdir = CollectionUtil.randomPickFromPersistentList(sources, _LiD);
                }

                //daca e fisier local
                if(ContentType.isMedia(pdir) && new File(pdir).exists()) {
                    final File pflf = new File(pdir);
                    set_current_path(pflf.getAbsolutePath());
                    _playing = true;
                    mPlayer.play(pflf.getAbsolutePath(), new Handler<String>() {
                        @Override
                        public void handle(String s) {
                            log.info(n + "] stopped " + pflf.getAbsolutePath());
                            handle_local_finished(c);
                        }
                    });
                    return;
                }

                //daca e format file (director sau fisier):
                else if (pdir.startsWith("file:")) {
                    String path = pdir.substring("file:".length());
                    File file = new File(apply_variables(path));
                    if (!file.exists()) {
                        log.warn(n + "] file " + file.getAbsolutePath() + " does not exist");
                        continue_pick(c, sources, retryIndex + 1);
                        return;
                    }

                    File pfl;
                    if(ContentType.isMedia(file)) {
                        pfl = file.getAbsoluteFile();
                    } else {
                        pfl = getRandomFileFromDirectory(file.getAbsolutePath());
                    }


                    if (pfl == null) {
                        log.info(n + "] null at " + pfl.getAbsolutePath());
                        continue_pick(c, sources, retryIndex + 1);
                        return;
                    }
                    log.info(n + "] play " + pfl.getAbsolutePath());
                    set_current_path(pfl.getAbsolutePath());
                   _playing = true;
                    mPlayer.play(pfl.getAbsolutePath(), new Handler<String>() {
                        @Override
                        public void handle(String s) {
                        handle_local_finished(c);
                        }
                    });
                    return;
                }

                //daca e url
                else if(ContentType.isHttpPath(pdir)) {
                    final long finalExp = exp;
                    _playing = false;

                    //default consideram ca e URL
                    NetworkUtil.pingUrl(pdir, new Handler<URLConnection>() {
                        @Override
                        public void handle(URLConnection huc) {
                            log.info(n + "] start stream show at " + DateUtil.nowHour() +" with url " + pdir + " and should end in " + strfMillis(finalExp));
                            clear_sys_props();
                            set_current_path(pdir);
                            _playing = true;
                            mPlayer.playStreamSync(pdir);
                            setup_stream_close(c, finalExp);
                        }
                    }, new Handler<Object>() {
                        @Override
                        public void handle(Object errTpl) {
                            log.error(n + "] iteration " + retryIndex + "check url " + pdir + " failed");
                            clear_sys_props();
                            _playing = false;
                            close_all_resources(new Handler<Object>() {
                                @Override
                                public void handle(Object o) {
                                play_from_list_of_strings(c, sources, retryIndex + 1);
                                }
                            });

                        }
                    });
                }

                else if(pdir.startsWith("vlc-list:")){

                    String path = pdir.substring("vlc-list:".length());
                    System.out.println(path);
                    if(!CommandRegistry.getInstance().containsCommand("vlc-open")) {
                        log.warn("Nu exista comanda vlc-open pentru deschidere playlisturi vlc");
                        continue_pick(c, sources, retryIndex + 1);
                        return;
                    }

                    File f = new File(path);
                    if(!f.exists()){
                        log.warn("Nu exista fisierul/directorul vlc-playlist" + f.getAbsolutePath());
                        continue_pick(c, sources, retryIndex + 1);
                        return;
                    }

                    CommandRegistry.getInstance().execute("vlc-open", new String[]{f.getAbsolutePath()});


                }
                else {
                    log.info(n + "] WTF faci cu " + pdir + "??????");
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
        _playing = false;
        closeTimer(t);
        mPlayer.stop(h);
    }
}