package com.arise.weland.impl;

import com.arise.canter.DefaultCommands;
import com.arise.core.models.Handler;
import com.arise.core.models.Tuple2;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.SYSUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OSProxies {
    private static final Mole log = Mole.getInstance(OSProxies.class);

    private static File getBinary(String p, String ... fixes){
        File f = new File(p);
        if (!f.exists()){
            //TODO
            return null;
        }
        return f;
    }

    public static void takeSnapshot(String id){
        File f = getBinary("/usr/bin/fswebcam", "apt-get install fswebcam", "dependency-manager solve fswebcam");
        String n = JARProxies.snapshotPath(id).getAbsolutePath();
        if (null == id) {
            SYSUtils.exec(new String[]{f.getAbsolutePath(), n});
        }
        else {
            SYSUtils.exec(new String[]{f.getAbsolutePath(), "-d", id, n});
        }
    }

    public static void findWebcamIds(Handler<List<Tuple2<String, String>>> h){
        File f = getBinary("/usr/bin/v4l2-ctl", "apt-get install v4l-utils");
        final int[] idx = {0};
        final List<Tuple2<String, String>> list = new ArrayList<>();
        SYSUtils.exec(new String[]{f.getAbsolutePath(), "--list-devices"}, new SYSUtils.ProcessLineReader() {
            @Override
            public void onStdoutLine(int line, String content) {
                String lines[] = content.split("\n");
                for (String x: lines){
                    x = x.trim();
                    if (x.startsWith("/dev")){
                        String id = idx[0] + "";
                        list.add(new Tuple2<>(id, x));
                        log.info("Found webcam "+ id + " = " + x );
                        idx[0]++;
                    }
                }
            }
        }, true, false);
        h.handle(list);
    }

    public static String getMasterVolume() {
        File f = getBinary("/usr/bin/amixer", "apt-get install alsa-utils");
        if (!FileUtil.exists(f)){
            log.error("Cannot read volume from empty file");
            return "0";
        }

        final String r[] = new String[]{null};
        SYSUtils.exec(new String[]{f.getAbsolutePath(), "get", "Master"}, new SYSUtils.ProcessLineReader() {
            @Override
            public void onStdoutLine(int line, String content) {
                String lines[] = content.split("\n");
                for (String x: lines){
                    x = x.trim().toLowerCase();
                    if (x.startsWith("mono:")){
                        try {
                            String pas = x.split("playback")[1].trim();
                            r[0] = pas.split(" ")[0];
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, true, false);
        return r[0];
    }

    public static void setVolume(String v) {
        File f = getBinary("/usr/bin/amixer", "apt-get install alsa-utils");
        if (f == null || !f.exists()){
            log.error("Cannot set volume on empty file");
            return;
        }
        DefaultCommands.PROCESS_EXEC.execute(f.getAbsolutePath(), "set", "Master", v);
//        SYSUtils.exec(f.getAbsolutePath(), "set", "Master", v);
    }


}
