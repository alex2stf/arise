package com.arise.weland.wrappers;

import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.StringUtil;

import java.io.File;
import java.io.IOException;

public class VLCWrapper {

    public static boolean open(String[] args) {
        String executablePath = args[0];
        File executable = new File(executablePath);
        if (!executable.exists()){
            return false;
        }
        boolean seemsVlc = executable.getName().equalsIgnoreCase("vlc")
                || executable.getName().equalsIgnoreCase("vlc.exe")
                || executable.getName().equalsIgnoreCase("VLCPortable.exe")
                ;
        if (!seemsVlc){
            return false;
        }
        String actualArgs[] = new String[]{
                executable.getAbsolutePath(),
                args[1]
//                ,"--reset-plugins-cache"
                , "--fullscreen"
                , "-I",
                "http",
                "--http-host=127.0.0.1",
                "--http-port=9090",
                "--http-password=arise",

        };

        System.out.println((StringUtil.join(actualArgs, " ")));

        SYSUtils.exec(actualArgs);


        return true;
    }
}
