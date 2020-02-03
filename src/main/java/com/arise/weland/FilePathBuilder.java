package com.arise.weland;

import com.arise.core.tools.ContentType;
import com.arise.core.tools.FileUtil;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by Alexandru on 2/1/2020.
 */
public class FilePathBuilder {


    public static void main(String[] args) {
        new FilePathBuilder().test();
    }


    Set<File> roots = new HashSet<>();

    public FilePathBuilder(){
        roots = new HashSet<>();
        roots.add(new File("D:\\"));
    }



    private void test() {
        FileUtil.recursiveScan(new File("D:\\"), new FileUtil.FileFoundHandler() {
            @Override
            public void foundFile(File file) {
                    if (ContentType.isPicture(file)){
                        add(file);
                    }
            }
        });
        for (String s: parents){
            System.out.println(s);
        }

//        addPath("D:\\$RECYCLE.BIN\\S-1-5-21-3465204109-2298250261-593176018-1000\\$R2J2KCV\\main\\resources\\icons\\actions");
//        addPath("D:\\$RECYCLE.BIN\\S-1-5-21-3465204109-2298250261-593176018-1000\\$R2J2KCV\\main\\resources\\icons\\filetree");
    }




    Set<String> parents = new HashSet<>();

    private void add(File file) {

        String filePath = file.getAbsolutePath();
        for (File root: roots){
            String rootPath = root.getAbsolutePath();
            if (filePath.startsWith(rootPath)){
                String removed = filePath.substring(rootPath.length());
                String name = removed.split(Pattern.quote(File.separator))[0];
                System.out.println(name + " >>> " + file.getAbsolutePath());
            }
        }
    }

    ;
}
