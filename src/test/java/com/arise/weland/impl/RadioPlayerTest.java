package com.arise.weland.impl;


import com.arise.canter.Command;
import com.arise.canter.CommandRegistry;
import com.arise.cargo.management.DependencyManager;
import com.arise.core.models.Handler;
import com.arise.core.tools.StringUtil;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static com.arise.core.tools.Assert.assertEquals;
import static java.lang.System.setOut;
import static java.lang.System.setProperty;

public class RadioPlayerTest {


    public static void main(String[] args) {

//        testFixed();

        RadioPlayer radioPlayer = new RadioPlayer();
        radioPlayer.loadShowsResourcePath("#radio_shows.json");

        setProperty("arise.forced.now", "2022-12-01 10:00:00");
        Show show = radioPlayer.getActiveShow();
        assertEquals("any_morning", show.n);
        assertEquals("https://live.rockfm.ro:8443/rockfm.aacp", show._s.get(0));


        setProperty("arise.forced.now", "2022-02-01 10:00:00");
        show = radioPlayer.getActiveShow();
        assertEquals("any_morning", show.n);
        assertEquals("https://live.rockfm.ro:8443/rockfm.aacp", show._s.get(0));

        setProperty("arise.forced.now", "2022-11-24 10:41:00");
        show = radioPlayer.getActiveShow();
        assertEquals("weekdays_10-40_11-40", show.n);

        setProperty("arise.forced.now", "2022-11-24 11:41:00");
        show = radioPlayer.getActiveShow();
        assertEquals("weekdays_11-40_12-20", show.n);


        setProperty("arise.forced.now", "2024-07-14 19:12:00");
        show = radioPlayer.getActiveShow();
        assertEquals("weekend_19-20", show.n);

        show.run(new Handler<Show>() {
            @Override
            public void handle(Show show) {
                System.out.println("wat");
            }
        });





    }


    public static void testFixed(){

        CommandRegistry.getInstance().loadJsonResource("commands_pi.json");



        CommandRegistry.getInstance().addCommand(new Command<Object>("process-exec") {
            @Override
            public Object execute(List<String> arguments) {
                System.out.println("Process exec " + StringUtil.join(arguments, " "));
                return null;
            }
        });

        CommandRegistry.getInstance().addCommand(new Command<Object>("browser-open") {
            @Override
            public Object execute(List<String> arguments) {
                System.out.println("Opened " + StringUtil.join(arguments, ""));
                return null;
            }
        });

        CommandRegistry.getInstance().addCommand(new Command<Object>("browser-close") {
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