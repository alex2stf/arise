package com.arise.weland.impl;

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
    public static void tryFsWebcam(Handler<List<Tuple2<String, String>>> h){
        File f = getBinary("/usr/bin/v4l2-ctl", "apt-get install v4l-utils");
        final int[] index = {0};
        final List<Tuple2<String, String>> list = new ArrayList<>();
        SYSUtils.exec(new String[]{f.getAbsolutePath(), "--list-devices"}, new SYSUtils.ProcessLineReader() {
            @Override
            public void onStdoutLine(int line, String content) {
                String lines[] = content.split("\n");
                for (String x: lines){
                    x = x.trim();
                    if (x.startsWith("/dev")){
                        String parts[] = x.split("/");
                        String id = parts[parts.length - 1];
                        System.out.println(id);
                        list.add(new Tuple2<>(index + "", id));
                        index[0]++;
                    }
                }
            }
        }, true, false);
    }
}
