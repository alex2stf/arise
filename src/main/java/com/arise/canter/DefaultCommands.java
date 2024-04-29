package com.arise.canter;

import com.arise.core.tools.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.List;

import static com.arise.core.tools.CollectionUtil.isEmpty;
import static com.arise.core.tools.CollectionUtil.toArray;
import static com.arise.core.tools.StringUtil.hasText;
import static com.arise.core.tools.StringUtil.join;
import static java.lang.Runtime.getRuntime;

public class DefaultCommands {



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
                    return sdf.format(Util.now());
                } catch (Exception e){
                    return Util.now() + "";
                }
            }
            return Util.now() + "";
        }

    };


    @Deprecated
    public static final Command<String> CMD_FIND_FILE_STREAM = new Command<String>("find-file-stream") {
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
                Mole.getInstance("PROC-EXEC").trace(join(args, " "));

                Process proc = getRuntime().exec(toArray(args));
                BufferedReader stdInput = new BufferedReader(new
                        InputStreamReader(proc.getInputStream()));

                BufferedReader stdError = new BufferedReader(new
                        InputStreamReader(proc.getErrorStream()));

                Mole.getInstance("PROC-EXEC").trace("\t|STDIN");
                String s = null;
                while ((s = stdInput.readLine()) != null) {
                    Mole.getInstance("PROC-EXEC").trace("\t|" + s);
                }

                Mole.getInstance("PROC-EXEC").trace("\t|  ERR");
                while ((s = stdError.readLine()) != null) {
                    Mole.getInstance("PROC-EXEC").trace("\t|" + s);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    };










}
