package com.arise.canter;

import com.arise.core.AppSettings;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.arise.core.tools.CollectionUtil.isEmpty;
import static com.arise.core.tools.CollectionUtil.toArray;
import static com.arise.core.tools.StringUtil.hasText;
import static com.arise.core.tools.StringUtil.join;
import static java.lang.Runtime.getRuntime;

public class Defaults {

    public static final Command<String> CMD_PRINT = new Command<String>("print") {
        @Override
        public String execute(List<String> arguments) {
            String joined = join(arguments, " ");
            System.out.println(joined);
            return joined;
        }
    };

    public static final Command<String> CMD_SUM = new Command<String>("sum") {


        @Override
        public String execute(List<String> arguments) {
            String a = arguments.get(0);
            String b = arguments.get(1);
            Integer ia;
            Integer ib;
            try {
                ia = Integer.valueOf(a);
                try {
                    ib = Integer.valueOf(b);
                    if (ia != null && ib != null){
                        return String.valueOf(ia + ib);
                    }
                } catch (Exception e){
                    ;;
                }
            } catch (Exception e){
                ;;
            }

            return a + b;
        }
    };

    public static final Command<String> CMD_GET_DATE = new Command<String>("get-date") {
        @Override
        public String execute(List<String> args) {
            if (!isEmpty(args) && hasText(args.get(0))){
                SimpleDateFormat sdf = new SimpleDateFormat(args.get(0));
                try {
                    return sdf.format(new Date());
                } catch (Exception e){
                    return new Date() + "";
                }
            }
            return new Date() + "";
        }

    };

    public static Command<String> CMD_FIND_FILE_STREAM = new Command<String>("find-file-stream") {
        @Override
        public String execute(List<String> args) {
            String path = args.get(0);
            InputStream inputStream = FileUtil.findStream(path);
            String name = FileUtil.getNameFromPath(path);
            File tmp = FileUtil.findSomeTempFile(name);

            tmp.deleteOnExit();
            FileOutputStream fo = null;
            try {
                fo = new FileOutputStream(tmp);
                StreamUtil.transfer(inputStream, fo);

            } catch (Exception e) {
                Mole.logWarn("Unable to find stream " + path, e);
            }
            Util.close(inputStream);
            Util.close(fo);
            return tmp.getAbsolutePath();
        }
    };

    public static final Command<String> CMD_VALID_FILE = new Command<String>("valid-file") {
        @Override
        public String execute( List<String> tests) {
            for (String s: tests){
                File f = new File(s);
                if(f.exists()){
                    return f.getAbsolutePath();
                }
            }
            Mole.getInstance("Command:" + getId()).warn("No valid exe found for " + join(tests, "]["));
            return null;
        }
    };


    public static final Command<Process> PROCESS_EXEC = new Command<Process>("process-exec") {



        @Override
        public Process execute(List<String> args) {
            try {
                return getRuntime().exec(toArray(args));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    };










}
