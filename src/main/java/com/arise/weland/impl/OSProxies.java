package com.arise.weland.impl;

import com.arise.cargo.management.Locations;
import com.arise.core.models.Handler;
import com.arise.core.models.Tuple2;
import com.arise.core.tools.Mole;
import com.arise.core.tools.SYSUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OSProxies {
    private static final Mole log = Mole.getInstance(OSProxies.class);

    private static File getBinary(String path, String ... fixes){
        return new File(path);
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
}
