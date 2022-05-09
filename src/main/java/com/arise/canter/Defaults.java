package com.arise.canter;

import com.arise.core.exceptions.LogicalException;
import com.arise.core.tools.CollectionUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.SYSUtils;
import com.arise.core.tools.StringUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Defaults {

    public static final Command<String> CMD_PRINT = new Command<String>("print") {
        @Override
        public String execute(Arguments arguments) {
            String joined = StringUtil.join(arguments.list(), " ");
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
            if (StringUtil.hasText(arguments.get(0))){
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

    public static final Command<String> PROCESS_EXEC = new Command<String>("process-exec") {
            @Override
            public String execute(Arguments arguments) {
                List<String> args = (List<String>) arguments.get("process-args");
                return  SYSUtils.exec(args).toJson();
            }

    };

    


    public static final Command<String> PROCESS_EXEC_WHEN_FOUND = new Command<String>("process-exec-when-found") {
        @Override
        public String execute(Arguments arguments) {

            if (CollectionUtil.isEmpty(arguments.getMapArgs())){
                throw new LogicalException("Cannot execute with empty arguments map");
            }


            Map<String, String> binaries;
            try {
                binaries = (Map<String, String>) arguments.get("binaries");
            } catch (Exception e){
                throw new LogicalException("properties binaries must be a valid map<string,string>", e);
            }
            boolean runSingleInstance = "true".equalsIgnoreCase(String.valueOf(arguments.get("singleInstance")));

            List<String> processArgs;
            try {
                processArgs = (List<String>) arguments.get("process-args");
            }catch (Exception e){
                throw new LogicalException("process arguments must be a valid list<string>", e);
            }


            File processExe;
            for (Map.Entry<String, String> entry: binaries.entrySet()){
                processExe = new File(entry.getValue());
                if (processExe.exists() && processExe.canExecute()){
                    return executeProcess(processExe, processArgs, runSingleInstance);
                }

            }

            return  null;
        }

        private String executeProcess(File path,  List<String> processArgs, boolean runSingleInstance){
            if (runSingleInstance){
                Command closeRunningProcess = this.getRegistry().getCommand("closeProcessByName");
                if (closeRunningProcess == null){
                    Mole.getInstance(this.getClass()).warn("to runSingleInstance must define command closeProcessByName" );
                } else {
                    String processName = path.getName();
                    closeRunningProcess.execute(processName);
                }
            }

            String args[] = new String[processArgs.size() + 1];
            args[0] = path.getAbsolutePath();
            for (int i = 0; i < processArgs.size(); i++){
                args[i+1] = processArgs.get(i);
            }
            return SYSUtils.exec(args).toJson();

        }

    };



    public static final Event EVT_SUCCESS = new Event("CMD_SUCCESS", new String[]{"FULL"});
    public static final Event EVT_FAIL = new Event("CMD_FAILED", new String[]{"FULL"});


}
