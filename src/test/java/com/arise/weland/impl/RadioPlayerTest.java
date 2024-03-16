package com.arise.weland.impl;


import com.arise.canter.Command;
import com.arise.canter.CommandRegistry;
import com.arise.cargo.management.DependencyManager;
import com.arise.core.models.Handler;
import com.arise.core.tools.StringUtil;
import com.arise.weland.impl.Show;
import com.arise.weland.model.MediaPlayer;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static com.arise.canter.Defaults.PROCESS_EXEC;
import static com.arise.core.tools.Assert.assertEquals;
import static java.lang.System.setProperty;

public class RadioPlayerTest {


    public static void main(String[] args) {

        testFixed();

        RadioPlayer radioPlayer = new RadioPlayer();
        radioPlayer.loadShowsResourcePath("#radio_shows.json");

        setProperty("arise.forced.now", "2022-12-01 10:00:00");
        Show show = radioPlayer.getActiveShow();
        assertEquals("weekdays_morning_all", show.n);
        assertEquals("https://live.rockfm.ro:8443/rockfm.aacp", show._s.get(0));


        setProperty("arise.forced.now", "2022-02-01 10:00:00");
        show = radioPlayer.getActiveShow();
        assertEquals("weekdays_morning_all", show.n);
        assertEquals("https://live.rockfm.ro:8443/rockfm.aacp", show._s.get(0));

        setProperty("arise.forced.now", "2022-11-24 10:41:00");
        show = radioPlayer.getActiveShow();
        assertEquals("weekdays_show_part_I", show.n);
        assertEquals("http://91.121.59.45:8013/stream", show._s.get(0));
        assertEquals("https://asculta.radioromanian.net/8800/stream?ver=587047", show._s.get(show._s.size() - 1));

        setProperty("arise.forced.now", "2022-11-24 11:41:00");
        show = radioPlayer.getActiveShow();
        assertEquals("weekdays_show_part_I", show.n);
        assertEquals("http://91.121.59.45:8013/stream", show._s.get(0));
        assertEquals("https://asculta.radioromanian.net/8800/stream?ver=587047", show._s.get(show._s.size() - 1));


        setProperty("arise.forced.now", "2022-12-02 10:50:00");
        show = radioPlayer.getActiveShow();
        assertEquals("weekdays_carols_1", show.n);
        assertEquals("file:/storage/emulated/0/Documents/arise-app/", show._s.get(0));
        assertEquals("https://craciun.radiomegahit.eu:8114/stream", show._s.get(show._s.size() - 1));


        setProperty("arise.forced.now", "2022-12-02 11:50:00");
        show = radioPlayer.getActiveShow();
        assertEquals("weekdays_carols_2", show.n);

        setProperty("arise.forced.now", "2022-11-24 12:41:00");
        show = radioPlayer.getActiveShow();
        assertEquals("weekdays_12_13", show.n);

        setProperty("arise.forced.now", "2022-11-24 13:23:00");
        show = radioPlayer.getActiveShow();
        assertEquals("weekdays_13_14", show.n);

        setProperty("arise.forced.now", "2022-12-02 12:41:00");
        show = radioPlayer.getActiveShow();
        assertEquals("weekdays_carols_12_13", show.n);

        setProperty("arise.forced.now", "2022-12-02 13:59:00");
        show = radioPlayer.getActiveShow();
        assertEquals("weekdays_carols_13_14", show.n);



        setProperty("arise.forced.now", "2022-12-02 14:59:00");
        show = radioPlayer.getActiveShow();
        assertEquals("relax_14_15", show.n);

        setProperty("arise.forced.now", "2022-11-24 14:15:00");
        show = radioPlayer.getActiveShow();
        assertEquals("relax_14_15", show.n);


        setProperty("arise.forced.now", "2022-12-02 15:49:00");
        show = radioPlayer.getActiveShow();
        assertEquals("relax_15_16", show.n);

        setProperty("arise.forced.now", "2022-11-24 15:35:00");
        show = radioPlayer.getActiveShow();
        assertEquals("relax_15_16", show.n);


        setProperty("arise.forced.now", "2022-11-24 16:35:00");
        show = radioPlayer.getActiveShow();
        assertEquals("weekdays_16_17", show.n);

        setProperty("arise.forced.now", "2022-12-06 16:35:00");
        show = radioPlayer.getActiveShow();
        assertEquals("weekdays_carols_16_17", show.n);


        setProperty("arise.forced.now", "2022-11-24 17:35:00");
        show = radioPlayer.getActiveShow();
        assertEquals("weekdays_17_18", show.n);

        setProperty("arise.forced.now", "2022-12-02 17:35:00");
        show = radioPlayer.getActiveShow();
        assertEquals("weekdays_carols_17_18", show.n);

        setProperty("arise.forced.now", "2022-11-24 18:35:00");
        show = radioPlayer.getActiveShow();
        assertEquals("weekdays_18_19", show.n);

        setProperty("arise.forced.now", "2022-12-02 18:35:00");
        show = radioPlayer.getActiveShow();
        assertEquals("weekdays_carols_18_19", show.n);


        setProperty("arise.forced.now", "2022-12-05 19:35:00");
        show = radioPlayer.getActiveShow();
        assertEquals("radio_shows_nightly", show.n);

        setProperty("arise.forced.now", "2022-11-21 19:35:00");
        show = radioPlayer.getActiveShow();
        assertEquals("radio_shows_nightly", show.n);

        setProperty("arise.forced.now", "2022-12-07 19:35:00");
        show = radioPlayer.getActiveShow();
        assertEquals("weekdays_carols_19_20", show.n);

        setProperty("arise.forced.now", "2022-11-24 19:35:00");
        show = radioPlayer.getActiveShow();
        assertEquals("weekdays_19_20", show.n);




    }


    public static void testFixed(){

        DependencyManager.getCommandRegistry().loadJsonResource("commands_pi.json");



        DependencyManager.getCommandRegistry().addCommand(new Command<Object>("process-exec") {
            @Override
            public Object execute(List<String> arguments) {
                System.out.println("Process exec " + StringUtil.join(arguments, " "));
                return null;
            }
        });

        DependencyManager.getCommandRegistry().addCommand(new Command<Object>("browser-open") {
            @Override
            public Object execute(List<String> arguments) {
                System.out.println("Opened " + StringUtil.join(arguments, ""));
                return null;
            }
        });

        DependencyManager.getCommandRegistry().addCommand(new Command<Object>("browser-close") {
            @Override
            public Object execute(List<String> arguments) {
                System.out.println("Closed " + StringUtil.join(arguments, ""));
                return null;
            }
        });
        RadioPlayer.getMediaPlayer();




        RadioPlayer radioPlayer = new RadioPlayer();

        Show s = new Show();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -1);

        String hh = "EACH_SECOND BETWEEN_10:00:00_AND_11:00:00";
        s.n = "Fallback_" + hh;
        s._h = hh;
        s._d = "monday_tuesday_wednesday_thursday_friday_sunday_saturday";
        s._s = Arrays.asList(
                "https://www.google.com/"
        );
        s._m = "stream";
        radioPlayer.addShow(s);

        s = new Show();
        hh = "EACH_SECOND BETWEEN_00:00:00_AND_24:00:00";
        s.n = "Test show " + hh;
        s._h = hh;
        s._d = "monday_tuesday_wednesday_thursday_friday_sunday_saturday";
        s._s = Arrays.asList(
                "file:/stf/test/local"
        );
        s._m = "stream";
        s._f = "Fallback_EACH_SECOND BETWEEN_10:00:00_AND_11:00:00";

        radioPlayer.addShow(s);


        radioPlayer.play();

        System.out.println(radioPlayer);
    }
}