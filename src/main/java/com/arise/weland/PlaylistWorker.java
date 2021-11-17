package com.arise.weland;

import com.arise.core.tools.*;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import static com.arise.core.tools.FileUtil.readLinesFromFile;


public class PlaylistWorker {

    private static File playlistDir() {
        File dir = new File(FileUtil.findAppDir() + File.separator + "pls");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }




    private static File upsertPlaylist(String name){
        String id = name.replaceAll("\\s+", "_");

        File dir = playlistDir();

        File f = new File(dir + File.separator + id + ".pl");
        if (!f.exists()){
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return f;
    }

    public static String listPlaylists() {
        File [] files = playlistDir().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".pl");
            }
        });
        if (files == null || files.length == 0){
            return "[]";
        }

        return "[" + StringUtil.join(files, ",",  new StringUtil.JoinIterator<File>() {
            @Override
            public String toString(File value) {
                return "\"" + value.getName() + "\"";
            }
        }) + "]";
    }

    public static void createPlaylist(String name) {
        upsertPlaylist(name);
    }

    public static void dropPlaylist(String name) {
    }

    private static final Mole log = Mole.getInstance(PlaylistWorker.class);

    public static void add(String playlistName, String value) {
        try {
            File pl = new File(playlistDir() + File.separator + playlistName);
            String content = FileUtil.read(pl);
            String parts[] = content.split("\n");
            if(parts != null && parts.length > 0){
                for(int i = 0; i < parts.length; i++){
                    if (parts[i].trim().equals(value)){
                        log.trace(value + " already found in " + playlistName);
                        return;
                    }
                }
            }
            content += value + "\n";
            FileUtil.writeStringToFile(pl, content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public static void setRunningPlaylist(String name) {
        AppCache.putString("cpLst", name);
        setIndex(0);
    }

    private static File getCurrentPlaylist(){
        if(AppCache.contains("cpLst")){
            File f = new File(playlistDir() + File.separator
                    + AppCache.getString("cpLst", null));
            if (f.exists()){
                return f;
            }
        }
        return null;
    }

    private static int getCurrentIndex(){
        return AppCache.getInt("cpLi", 0);
    }

    private static void setIndex(int i){
        AppCache.putInt("cpLi", i);
    }


    public static String getPlaylist(String name) {
        File f = new File(playlistDir() + File.separator + name);
        return f.getName();
    }



    public static String getNextFromCurrentPlaylist() {
        File cp = getCurrentPlaylist();
        if (null == cp){
            return null;
        }
        try {
            List<String> lines = readLinesFromFile(cp);
            if (!CollectionUtil.isEmpty(lines)){
                int index = getCurrentIndex();

                if (index > lines.size() - 1 ){
                    return null; //at the end it stops ???
                }
                String path = lines.get(index);
                setIndex(index + 1);
                return path;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
}
