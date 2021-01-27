package com.arise.weland.impl.unarchivers;

import com.arise.cargo.management.Dependencies;
import com.arise.cargo.management.Dependency;
import com.arise.cargo.management.DependencyManager;
import com.arise.core.tools.CollectionUtil;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;
import com.arise.weland.IDGen;
import com.arise.weland.dto.ContentInfo;
import com.arise.weland.impl.PCDecoder;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MediaInfoSolver {
    static  DependencyManager.Resolution mediaInfoResolution;
    static  DependencyManager.Resolution mntResolution;

//    private static final Mole log = Mole.getInstance(MediaInfoSolver.class);

    public static void solve(final  ContentInfo info){
        solveInfo(info);
        solveThumbnail(info);
    }

    private static void solveThumbnail(final ContentInfo info) {
        if (mntResolution == null){
            return;
        }

        File mntExe = new File(mntResolution.uncompressed(), "mtn.exe");
        File outDir = PCDecoder.thumbnailsDirectory();
        if (!outDir.exists()){
            outDir.mkdirs();
        }
        String suffix = "-arise-app-gen.jpg";



        SYSUtils.exec(
                new String[]{
                        mntExe.getAbsolutePath(),
                        "-P", //pause off
                        "-I", //skip text info
                        info.getPath(),
                        "-O", //output directory
                        outDir.getAbsolutePath(),
                        "-o",
                        suffix

                },
                new SYSUtils.ProcessOutputHandler() {
                    @Override
                    public void handle(InputStream inputStream) {
//                        System.out.println(StreamUtil.toString(inputStream));
                    }

                    @Override
                    public void handleErr(InputStream errorStream) {

                    }
                },
                true, false
        );



        File generated[] = outDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(info.getName());
            }
        });
        if (CollectionUtil.isEmpty(generated)){
            return;
        }
        if (generated.length == 1){
            assignFile(info, generated[0]);
            return;
        }
        File selected = null;
        for (int i = 0; i < generated.length; i++){
            File f = generated[i];

            if (f.getName().endsWith(suffix)){
                f.delete();
            } else if (selected == null){
                selected = generated[i];
            }
            else {
                generated[i].delete();
            }
        }
        File dest = new File(
                selected.getParentFile(),
                IDGen.parsePath(selected.getName())
        );


        selected.renameTo(dest);
        assignFile(info, dest);
    }

    private static void assignFile(ContentInfo info, File file) {
        info.setThumbnailId(file.getName());
    }


    /**
     * DURATION duration : 7s 8ms
     * DURATION duration : 7s 8ms
     * WIDTH width : 640 pixels
     * HEIGHT height : 360 pixels
     * DURATION duration : 7s 8ms
     * @param info
     */
    public static void solveInfo(final ContentInfo info) {
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
//                                log.info(lowcase);
                                info.setWidth(StringUtil.extractInt(lowcase, info.getWidth()));
                            }
                            else if (lowcase.startsWith("height")){
//                                log.info( lowcase);
                                info.setHeight(StringUtil.extractInt(lowcase, info.getHeight()));
                            }
                            else if (lowcase.startsWith("duration")){
//                                log.info(lowcase);
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
//                                log.info("miliseconds : " + duration);
                            }

                        }
                    }

                    @Override
                    public void handleErr(InputStream errorStream) {

                    }
                }, true, false

        );
    }

    public static void load() {
        mntResolution = DependencyManager.solveSilent(Dependencies.MNT_CLI_WIN_32);
        mediaInfoResolution = DependencyManager.solveSilent(Dependencies.MEDIA_INFO_CLI_WIN32);
    }

}
