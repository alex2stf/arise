package com.arise.canter;

import com.arise.core.tools.CollectionUtil;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.SYSUtils;
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

import static com.arise.core.tools.StringUtil.join;

public class Defaults {

    public static final Command<String> CMD_PRINT = new Command<String>("print") {
        @Override
        public String execute(Arguments arguments) {
            String joined = join(arguments.list(), " ");
            System.out.println(joined);
            return joined;
        }

    };

    public static final Command<String> CMD_SUM = new Command<String>("sum") {
        @Override
        public String execute(Arguments arguments) {
            String a = arguments.get(0);
            String b = arguments.get(1);
            Integer ia = null;
            Integer ib = null;
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
        public String execute(Arguments arguments) {
            if (arguments.hasData() && StringUtil.hasText(arguments.get(0))){
                SimpleDateFormat sdf = new SimpleDateFormat(arguments.get(0));
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
        public String execute(Arguments arguments) {
            List<String> args = arguments.getListArgs();
            String path = args.get(0);
            InputStream inputStream = FileUtil.findStream(path);
            String name = FileUtil.getNameFromPath(path);
            File tmp = FileUtil.findSomeTempFile(name);

            tmp.deleteOnExit();
            FileOutputStream fo = null;
            try {
                fo = new FileOutputStream(tmp);
                StreamUtil.transfer(inputStream, fo);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Util.close(inputStream);
            Util.close(fo);
            return tmp.getAbsolutePath();
        }
    };

    public static final Command<String> CMD_VALID_FILE = new Command<String>("valid-file") {
        @Override
        public String execute(Arguments arguments) {
            List<String> tests = arguments.getListArgs();
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

    public static final Command<String> PROCESS_EXEC_PRINT = new Command<String>("process-exec-print") {
        @Override
        public String execute(Arguments arguments) {
            String res = PROCESS_EXEC.execute(arguments);
            System.out.println(res);
            return res;
        }
    };

    public static final Command<String> PROCESS_EXEC = new Command<String>("process-exec") {
        @Override
        public String execute(Arguments arguments) {
            List<String> args = arguments.getListArgs();

            File stdOutFile = null;
            int lastIndex = args.size();
            for(int i = 0; i < args.size(); i++){
                String part = args.get(i);
                if(part.startsWith("STDOUT>")){
                    stdOutFile = new File(part.substring("STDOUT>".length()).trim());
                    lastIndex = i;
                    break;
                }
            }


            String[] actualArgs = new String[lastIndex];
            for(int j = 0; j < lastIndex; j++){
                actualArgs[j] = args.get(j);
            }


            final File finStdFile = stdOutFile;
            if(finStdFile != null && finStdFile.exists()) {
                finStdFile.delete();
            }



            final StringBuilder sb = new StringBuilder();
            final boolean[] returnSb = {false};
            SYSUtils.exec(CollectionUtil.toArray(args), new SYSUtils.ProcessLineReader() {

                @Override
                public void onErrLine(int line, String content) {
                    System.out.println(content);
                }

                @Override
                public void onStdoutLine(int line, String content) {
                    if (finStdFile != null) {
                        try {
                            FileUtil.appendNewLineToFile(content, finStdFile);
                        } catch (IOException e) {
                            e.printStackTrace(); //TODO fix this error catch
                        }
                        returnSb[0] = false;
                    } else {
                        sb.append(content);
                        returnSb[0] = true;
                    }
                }
            }, true, false);
            if (returnSb[0]) {
                return sb.toString();
            }
            return finStdFile != null ? finStdFile.getAbsolutePath() : "null";
        }

    };

    






    public static final Event EVT_SUCCESS = new Event("CMD_SUCCESS", new String[]{"FULL"});
    public static final Event EVT_FAIL = new Event("CMD_FAILED", new String[]{"FULL"});


}
