package com.arise.core.tools;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@Deprecated
public class FS {


    private Map<String, Watcher> watcherMap = new HashMap<>();

    Watcher getWatcher(File f){
        if (!watcherMap.containsKey(f.getAbsolutePath())){
            watcherMap.put(f.getAbsolutePath(), new Watcher(f));
        }
        return watcherMap.get(f.getAbsolutePath());
    }

    public void keepDirSync(final File source, final File destination,  long delay){

//        System.out.println(source.getAbsolutePath() + "------" + new Date());

        FileUtil.recursiveScan(source, new FileUtil.FileFoundHandler() {
            @Override
            public void foundFile(File file) {
                getWatcher(file).track(new WatchEvent() {
                    @Override
                    public void on(String id, File f) {
                        String part = f.getAbsolutePath().replace(source.getAbsolutePath(), "");
                        File destFile = new File(destination.getAbsolutePath() + part);
                        System.out.println("copy " + f.getAbsolutePath() + " to " + destFile.getAbsolutePath());
                        try {
                            Files.copy(f.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        keepDirSync(source, destination, delay);
    }

    public static void main(String[] args) {
        if ("--keep-dir-sync".equals(args[0])){
            final File source = new File(args[1]);
            final File destination = new File(args[2]);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    new FS().keepDirSync(source, destination, 1000 * 1);
                }
            }).start();
            System.out.println("" + args[1] +" with " +args[2]);
        }

    }

    private class Watcher {
        private final File f;
        private boolean exists = true;
        private long lastModified;
        private String path;

        public Watcher(File f) {
            this.f = f;
            exists = f.exists();
            path = f.getAbsolutePath();
            lastModified = f.lastModified();
        }

        private void dispatch(WatchEvent event, String id, File f){
            if (event != null){
                event.on(id, f);
            }
        }
        public void track(WatchEvent event) {
            if (exists != f.exists()){
                dispatch(event, "removed", f);
                exists = f.exists();
            }
            if (path != f.getAbsolutePath()){
                dispatch(event, "renamed", f);
                path = f.getAbsolutePath();
            }

            if (lastModified != f.lastModified()){
                dispatch(event, "modified", f);
                lastModified = f.lastModified();
            }


        }
    }


    interface WatchEvent{
        void on(String id, File f);
    }
}
