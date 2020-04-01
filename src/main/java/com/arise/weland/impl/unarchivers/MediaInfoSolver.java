package com.arise.weland.impl.unarchivers;

import com.arise.cargo.management.Dependencies;
import com.arise.cargo.management.DependencyManager;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;
import com.arise.weland.dto.ContentInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MediaInfoSolver {
    static  DependencyManager.Resolution mediaInfoResolution;

    private static final Mole log = Mole.getInstance(MediaInfoSolver.class);

    /**
     * DURATION duration : 7s 8ms
     * DURATION duration : 7s 8ms
     * WIDTH width : 640 pixels
     * HEIGHT height : 360 pixels
     * DURATION duration : 7s 8ms
     * @param info
     */
    public static void solve(final ContentInfo info) {
        if (mediaInfoResolution == null){
            return;
        }

        File mediaExe = new File(mediaInfoResolution.uncompressed(), "MediaInfo.exe");

        SYSUtils.exec(
                new String[]{mediaExe.getAbsolutePath(), info.getPath()},
                new SYSUtils.ProcessOutputHandler() {
                    @Override
                    public void handle(InputStream inputStream) {
                        String content = StreamUtil.toString(inputStream);
                        String lines[] = content.split("\n");
                        for (String line: lines){
                            //System.out.println("---" + line);
                            String lowcase = line.trim().toLowerCase().replaceAll("\\s+", " ");
                            if (lowcase.startsWith("width")){
                                log.info(lowcase);
                                info.setWidth(StringUtil.extractInt(lowcase, info.getWidth()));
                            }
                            else if (lowcase.startsWith("height")){
                                log.info( lowcase);
                                info.setHeight(StringUtil.extractInt(lowcase, info.getHeight()));
                            }
                            else if (lowcase.startsWith("duration")){
                                log.info(lowcase);
                                String parts[] = lowcase.split("\\s+");
                                List<Integer> integers = new ArrayList<>();
                                for (String s: parts){
                                    Integer in = StringUtil.extractInteger(s);
                                    if (in != null){
                                        integers.add(in);
                                    }
                                }

                                Collections.reverse(integers);
                                int duration = 0;
                                if (integers.size() > 0){
                                    duration = integers.get(0);
                                }
                                if (integers.size() > 1){
                                    duration += 1000 * integers.get(1);
                                }
                                //minutes
                                if (integers.size() > 2){
                                    duration += 1000 * 60 * integers.get(2);
                                }
                                //hours
                                if (integers.size() > 3){
                                    duration += 1000 * 60 * 60 * integers.get(2);
                                }
                                info.setDuration(duration);
                                log.info("miliseconds : " + duration);
                            }

                        }
                    }

                    @Override
                    public void handleErr(InputStream errorStream) {

                    }
                }, true, false

        );
        System.out.println(mediaInfoResolution.uncompressed());
    }

    public static void load() {
        if (mediaInfoResolution != null){
            return;
        }
        DependencyManager.Resolution mediaInfoResolution1;

        try {
            mediaInfoResolution1 = DependencyManager.solve(Dependencies.MEDIA_INFO_CLI_WIN32);
        } catch (IOException e) {
            e.printStackTrace();
            mediaInfoResolution1 = null;
        }
        mediaInfoResolution = mediaInfoResolution1;
    }
}
