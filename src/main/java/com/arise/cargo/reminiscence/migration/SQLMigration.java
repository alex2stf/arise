package com.arise.cargo.reminiscence.migration;

import com.arise.core.tools.Util;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;


public class SQLMigration {


    private File mainFile;
    private File versionFile;
    private boolean verbose = true;
    private Properties cache = new Properties();
    private String taskId = "";
    private ExceptionHandler exceptionHandler;
    private StatementHandler statementHandler;

    private static boolean  loanProps(File f, Properties p){
        InputStream i = null; boolean s = false;
        try {
            i = new FileInputStream(f);
            p.load(i);
            s = true;
        } catch (Exception e) {
            s = false;
        } finally {
            if(i != null){
                try {
                    i.close();
                } catch (IOException e) {
                    ;;
                }
            }
        }
        return s;
    }

    public static void main(String[] args) {
        String directory = args[0];
        String driver = args[1];
        String jdbcUrl = args[2];
        String userName = args[3];
        String password = args[4];
        String taskId = args[5];
        String versionFile = null;
        String versionTable = null;
        String exHandling = null;
        boolean verbose = false;

        if (args.length > 6){
            exHandling = (args[6]);
        }

        System.out.println(".................taskId [" + taskId + "]");
        System.out.println("...using root directory [" + directory + "]");
        System.out.println("...........using driver [" + driver + "]");
        System.out.println("...... exceptionHandler [" + exHandling + "]");



        if (args.length > 7){
            String arg = args[7];
            if (arg.startsWith("TABLE:")){
                versionTable = arg.substring("TABLE:".length());
                System.out.println(".............versionTable [" + versionFile + "]");

            } else {
                versionFile = arg;
                System.out.println(".............versionFile [" + versionFile + "]");
            }

        }

        if (args.length > 8){
            verbose = "true".equals(args[8]);
            System.out.println("................ verbose [" + verbose + "]");
        }


        System.exit(0);


        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        Connection con = null;
        SQLMigration sqlMigration = new SQLMigration();
        try {
            System.out.println("............... jdbcUrl [" + jdbcUrl + "]");
            System.out.println(".............. userName [" + userName + "]");
            con = DriverManager.getConnection(
                    jdbcUrl,userName,password);
        } catch (SQLException e) {
            e.printStackTrace();
            sqlMigration.closeConnection(con);
            System.exit(-1);
        }



        sqlMigration
                .setTaskId(taskId)
                .setExceptionHandler(fromPropsFile(exHandling))
                .setVersionFile(versionFile)
                .setFile(directory)
                .setVerbose(verbose)
                .execute(con)
                .saveVersion();
        sqlMigration.closeConnection(con);

    }

    public SQLMigration setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    public StatementHandler getStatementHandler() {
        return statementHandler;
    }

    public SQLMigration setStatementHandler(StatementHandler statementHandler) {
        this.statementHandler = statementHandler;
        return this;
    }

    private SQLMigration setTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public SQLMigration setFile(String file) {
        return setFile(new File(file));
    }

    private SQLMigration setFile(File file) {
        mainFile = file;
        return this;
    }

    private SQLMigration setVerbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    private void sout(String msg){
        if (verbose){
            System.out.println(msg);
        }
    }

    public SQLMigration execute(Connection sqlConnection){
        loadVersion();

        if (!mainFile.isDirectory() && mainFile.isFile()){
            execute(mainFile, sqlConnection);
            return this;
        }
        List<File> files = Arrays.asList(mainFile.listFiles());
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1 != null && o1.getName() != null && o2 != null){
                    return o1.getName().compareTo(o2.getName());
                }
                return 0;
            }
        });
        for (File file: files){
            execute(file, sqlConnection);
        }
        return this;
    }

    private void loadVersion() {
        if (versionFile == null){
            return;
        }
        versionFile = new File(versionFile.getAbsolutePath() + ".properties");
        if (!versionFile.exists()){
            try {
                versionFile.createNewFile();
            } catch (IOException e) {
                System.out.println("failed to create version file " + versionFile.getAbsolutePath());
                versionFile = null;
                return;
            }
        }
        if(!loanProps(versionFile, cache)){
            versionFile = null;
        }
    }

    private void execute(File file, Connection sqlConnection){
        sout("\n|---------------------------------------------");
        sout("|             " + file.getAbsolutePath());
        Parser sqlScriptParser = new Parser()
                .defineCommentInit("--")
                .parse(file);

        for (String command: sqlScriptParser.getCommands()){
            if (commandVersionExecuted(command)){
                sout("|-[SKIP:] " + command);
                continue;
            }
            sout("| " + command);

            if (statementHandler != null){
                statementHandler.onStatement(file, command);
            }

            Statement statement = null;
            String stat = "UNSET";
            if (sqlConnection != null){
                try {
                    statement = sqlConnection.createStatement();
                    statement.executeUpdate(command);
                    stat = "ok";
                } catch (SQLException e) {
                    stat = "ERR";
                    if (exceptionHandler != null){
                        exceptionHandler.onError(e, taskId);
                    }
                }
            }
            sout("|-----------------------------------------");
            closeStatement(statement);
            versionCommand(command, stat);
        }

    }

    private void versionCommand(String command, String stat) {

        cache.put(key(command), stat);

    }

    private boolean commandVersionExecuted(String command) {
        return cache.getProperty(key(command)) != null;
    }

    private String key(String command){
        return String.valueOf(command.hashCode()).replaceAll("-", "C");
    }

    public void closeConnection(Connection connection){
        Util.close(connection);
    }

    private void closeStatement(Statement statement){
       Util.close(statement);
    }

    private SQLMigration setVersionFile(String path) {
        if(path != null && !path.trim().isEmpty()) {
            versionFile = new File(path);
        }
        return this;
    }


    public SQLMigration saveVersion() {
        if (versionFile == null){
            return this;
        }

        OutputStream output = null;
        try {
            output = new FileOutputStream(versionFile);
            cache.store(output, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (output != null){
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return this;
    }

    private interface ExceptionHandler {


        void onError(SQLException t, String taskId);
    }

    private static ExceptionHandler fromPropsFile(String f) {
        Properties properties = new Properties();
        loanProps(new File(f), properties);
        return fromProps(properties);
    }

    private static ExceptionHandler fromProps(final Properties properties) {
        return new ExceptionHandler() {

            private int callb(String todo, SQLException t){
                if ("warn".equalsIgnoreCase(todo)){
                    System.out.println(t.getSQLState() + t.getErrorCode() + t.getMessage());
                    return 1;
                }
                else if ("skip".equalsIgnoreCase(todo)){
                    return 1;
                }
                else if ("exit".equalsIgnoreCase(todo)){
                    t.printStackTrace();
                    System.exit(-1);
                }
                else if ("exit0".equalsIgnoreCase(todo)){
                    t.printStackTrace();
                    System.exit(0);
                }
                else if ("stack".equalsIgnoreCase(todo)){
                    t.printStackTrace();
                    return 1;
                }
                return 0;
            }

            @Override
            public void onError(SQLException t, String taskId) {
                String key = taskId + "." + t.getSQLState();
                String prop = properties.getProperty(key);
                int executed = 0;
                if (prop != null){
                    String [] todos = prop.split(",");
                    for (String todo: todos){
                        executed += callb(todo, t);
                    }
                }
                if (executed == 0){
                    String default_behaviour = properties.getProperty(taskId + "._default");
                    if (default_behaviour != null){
                        for (String todo: default_behaviour.split(",")){
                            callb(todo, t);
                        }
                    } else {
                        t.printStackTrace();
                    }
                }
            }
        };
    }

    public static class Parser{
        private String sep = ";";
        private String commIni = "#";
        private String lineSep = "\n";
        private StringBuilder sb = new StringBuilder();

        public String getSeparator() {
            return sep;
        }

        public void defineSeparator(String s) {
            this.sep = s;
        }

        public Parser parse(File file) {

            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader((file)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            String line = null;
            try {
                while((line = reader.readLine()) != null) {
                    digestLine(line);
                }

            }
            catch (Exception ex){
                ex.printStackTrace();
            }
            finally {
                if (reader != null){
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
            return this;
        }

        public Parser parse(String s) {
            String[] lines = s.split(lineSep);
            for (String line: lines){
                digestLine(line);
            }
            return this;
        }

        public  List<String> getCommands(){
            return extractCommands(sb.toString());
        }




        public  List<String> extractCommands(String input){
            List<String> response = new ArrayList<>();

            int n1 = 0;
            int n2 = 0;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < input.length(); i++){
                char c = input.charAt(i);
                char L = i > 0 ? input.charAt(i - 1) : '\0';
                if (c == '\'' && L != '\\'){
                    n1++;
                    sb.append(c);
                }
                else if( c == '"' && L != '\\'){
                    n2++;
                    sb.append(c);
                }
                else if(c == ';' && n1 % 2 == 0 && n2 % 2 == 0){
                    response.add(sb.toString().trim().replaceAll(" +", " "));
                    sb = new StringBuilder();
                } else {
                    sb.append(c);
                }

            }
            return response;
        }

        public void digestLine(String line){
            if (line == null){
                return;
            }
            if (!line.startsWith(commIni)){
                sb.append(line);
                sb.append(" ");
            }

        }

        public Parser defineCommentInit(String s) {
            this.commIni = s;
            return this;
        }

    }

    public interface StatementHandler {
        void onStatement(File file, String statement);
    }
}
