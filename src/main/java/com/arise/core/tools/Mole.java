package com.arise.core.tools;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import static java.lang.Class.forName;
import static java.lang.String.valueOf;


/**
 * Created by alex on 27/11/2017.
 */
public class Mole {

    private static Class<? extends Delegate> mainDelegate;
    private static Set<Appender> appenders = new HashSet<>();


    private static final String ANDROID_LOG1_CLASS = "android.util.Log";
    private static final String APACHE_LOG4J_CLASS = "org.apache.log4j.Logger";

    private static <T extends Delegate> void setMainDelegate(Class<T> tClass){
        mainDelegate = tClass;
    }

    public static void addAppender(Appender appender){
        appenders.add(appender);
    }

    private synchronized static void searchDelegate(){
        if (mainDelegate == null && ReflectUtil.classExists(ANDROID_LOG1_CLASS)){
            setMainDelegate(AndroidLogDelegate.class);
        }

        else if (mainDelegate == null && ReflectUtil.classExists(APACHE_LOG4J_CLASS)){
            setMainDelegate(Log4JDelegate.class);
        }

//        if (mainDelegate == null){
//            setMainDelegate(JUtilDelegate.class);
//        }

        if (mainDelegate == null){
            setMainDelegate(SysOutDelegate.class);
        }
    }






    public static Mole getInstance(Class c){
        return new Mole(c);
    }
    public static Mole getLogger(Class c){
        return new Mole(c);
    }
    public static Mole getLogger(String n){
        return new Mole(n);
    }
    public static Mole getInstance(String name){
        return new Mole(name);
    }

    private Delegate delegate;
    private String id;

    private static final Map<String, Boolean> oncs = new ConcurrentHashMap<>();

    public Mole(String name){
        searchDelegate();
        try {
            delegate = mainDelegate.getDeclaredConstructor(Object.class).newInstance(name);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        id = name;
    }

    public Mole(Class clazz){
        searchDelegate();
        try {
            delegate = mainDelegate.getDeclaredConstructor(Object.class).newInstance(clazz);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        id = clazz.getName();
    }

    public static void todo(String ... args) {
        logInfo("\t| -------------------------------");
        logInfo("\t| TODO (method not implemented)");
        StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        if (ste != null || ste.length > 1){
            StackTraceElement as = ste[2];
            logInfo( "\t| " + as.getClassName() + "#" + as.getMethodName() + "(?) @" + as.getLineNumber() );
        }
        if (args != null && args.length > 0) {
            logInfo("\t| " + StringUtil.join(args, " "));
        }
        logInfo("\t| -------------------------------");
    }




    private void callAppenders(Bag bag, Object ... args){
        for (Appender appender: appenders){
            appender.append(
                    id , bag,
                    StringUtil.join(args, " ")
            );
        }
    }


    public void log(Object ... args){
        delegate.log(args);
        callAppenders(Bag.LOG, args);
    }

    public void trace(Object ... args) {
        delegate.trace(args);
        callAppenders(Bag.TRACE, args);
    }


    public void debug(Object ... args) {
        delegate.debug(args);
        callAppenders(Bag.DEBUG, args);
    }


    public void error(Object ... args) {
        delegate.error(args);
        callAppenders(Bag.ERROR, args);
    }


    public static void logInfo(Object ... args) {
        Mole.getInstance(Mole.class).info(args);
    }

    public static void logWarn(Object ... args) {
        Mole.getInstance(Mole.class).warn(args);
    }

    public void info(Object ... args) {
        delegate.info(args);
        callAppenders(Bag.INFO, args);
    }

    public void warn(Object ... args) {
        delegate.warn(args);
        callAppenders(Bag.WARN, args);
    }

    public void fatal(Object ... args) {
        delegate.fatal(args);
        callAppenders(Bag.FATAL, args);
    }

    public boolean isTraceEnabled() {
        return delegate.isTraceEnabled();
    }

    public boolean isInfoEnabled() {
        return delegate.isInfoEnabled();
    }

    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

    public void warning(Object ... args) {
        warn(args);
    }

    public void w(Object ... args) {
        warn(args);
    }

    public void d(Object ...args) {
        debug(args);
    }

    public void i(Object ... args) {
        info(args);
    }

    public void e(Object ... args) {
        error(args);
    }

    public void verbose(Object ... args) {
        trace(args);
    }

    public void once(Bag level, Object ... args) {
        String id = UUID.nameUUIDFromBytes(StringUtil.join(args, "").getBytes()).toString();
        if (oncs.containsKey(id)){
            return;
        }
        info(args);
        oncs.put(id, true);
    }


    public enum Bag {
        LOG, WARN, ERROR, INFO, FATAL, TRACE, DEBUG;
    }

    public interface Appender {
        void append(String id, Bag bag, String text);
    }

    public static abstract class Delegate{
        public Delegate(Object arg) throws Exception {
        }
        public abstract boolean error(Object ... args);
        public abstract boolean info(Object ... args);
        public abstract boolean trace(Object ... args);
        public abstract boolean fatal(Object ... args);
        public abstract boolean warn(Object ... args);
        public abstract boolean debug(Object ... args);
        public abstract boolean log(Object ... args);
        public abstract boolean isTraceEnabled();
        public abstract boolean isInfoEnabled();
        public abstract boolean isDebugEnabled();
    }


    private static class JUtilDelegate extends Delegate {
        java.util.logging.Logger logger;

        public JUtilDelegate(Object arg) throws Exception {
            super(arg);
            logger = java.util.logging.Logger.getLogger(valueOf(arg));
        }


        private boolean callLevel(Level level, Object ... args){
            if (args == null){
                return true;
            }
            if (args.length == 1){
                if (args[0] instanceof Throwable){
                    logger.log(level, StringUtil.dump(args[0]));
                } else {
                    logger.log(level, valueOf(args[0]));
                }

            } else if (args.length == 2 && args[1] instanceof Throwable){
                logger.log(level, valueOf(args[0]), (Throwable) args[1]);
            } else {
                logger.log(level, StringUtil.join(args, ""));
            }
            return true;

        }

        @Override
        public boolean error(Object... args) {
            return callLevel(Level.SEVERE, args);
        }

        @Override
        public boolean info(Object... args) {
            return callLevel(Level.FINER, args);
        }

        @Override
        public boolean trace(Object... args) {
            return callLevel(Level.FINEST, args);
        }

        @Override
        public boolean fatal(Object... args) {
            return callLevel(Level.SEVERE, args);
        }

        @Override
        public boolean warn(Object... args) {
            return callLevel(Level.WARNING, args);
        }

        @Override
        public boolean debug(Object... args) {
            return callLevel(Level.FINE, args);
        }

        @Override
        public boolean log(Object... args) {
            if (args.length > 1 && args[0] instanceof Level){

            }
            return false;
        }

        @Override
        public boolean isTraceEnabled() {
            return  logger.isLoggable(Level.FINEST);
        }

        @Override
        public boolean isInfoEnabled() {
            return logger.isLoggable(Level.INFO);
        }

        @Override
        public boolean isDebugEnabled() {
            return logger.isLoggable(Level.FINE);
        }
    }



    private static class Log4JDelegate extends Delegate{

        private Object log4J;
        private String clzName = APACHE_LOG4J_CLASS;
        private Class clzz;

        public Log4JDelegate(Object arg) throws Exception {
            super(arg);
            clzz = forName(clzName);
            if (clzz == null){
                throw new RuntimeException("failed to refreshUI");
            }
            if (arg instanceof String){
                log4J = clzz.getDeclaredMethod("getLogger", String.class).invoke(null, arg);
            } else if (arg instanceof Class){
                log4J = clzz.getDeclaredMethod("getLogger", Class.class).invoke(null, arg);
            }

            if (log4J == null){
                throw new RuntimeException("failed to refreshUI");
            }
        }


        private boolean invoke(String method, Object ... args){
            if (args == null){
                return true;
            }
            try {
                if (args.length == 1){
                    clzz.getMethod(method, Object.class).invoke(log4J, args[0]);
                }
                else if (args.length == 2 && args[1] instanceof Throwable){
                    clzz.getMethod(method, Object.class, Throwable.class).invoke(log4J, args[0], args[1]);
                }
                else {
                    clzz.getMethod(method, Object.class).invoke(log4J, StringUtil.join(args, ","));
                }
            }catch (Exception x){
                return false;
            }
            return true;
        }


        @Override
        public boolean error(Object... args) {
            return invoke("error", args);
        }

        @Override
        public boolean info(Object... args) {
            return invoke("info", args);
        }

        @Override
        public boolean trace(Object... args) {
            return invoke("trace", args);
        }

        @Override
        public boolean fatal(Object... args) {
            return invoke("fatal", args);
        }

        @Override
        public boolean warn(Object... args) {
            return invoke("warn", args);
        }

        @Override
        public boolean debug(Object... args) {
            return invoke("debug", args);
        }

        @Override
        public boolean log(Object... args) {
            return invoke("debug", args);
        }

        public boolean invokeBoolean(String methodName, boolean defVal){
            try {
                return (boolean) clzz.getMethod(methodName).invoke(log4J);
            } catch (IllegalAccessException e) {
                return defVal;
            } catch (InvocationTargetException e) {
                return defVal;
            } catch (NoSuchMethodException e) {
                return defVal;
            }
        }

        @Override
        public boolean isTraceEnabled() {
            return invokeBoolean("isTraceEnabled", false);
        }

        @Override
        public boolean isInfoEnabled() {
            return invokeBoolean("isInfoEnabled", false);
        }

        @Override
        public boolean isDebugEnabled() {
            return invokeBoolean("isDebugEnabled", false);
        }
    }


    private static String getName(Object arg){
        if (arg instanceof Class){
            return  ((Class) arg).getSimpleName();
        } else {
            return valueOf(arg);
        }
    }


    private static class SysOutDelegate extends Delegate {

        private String name;

        private StringUtil.JoinIterator<Object> joinIterator = new StringUtil.JoinIterator<Object>() {
            @Override
            public String toString(Object o) {
                if (o instanceof Throwable){
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    ((Throwable) o).printStackTrace(pw);
                    return sw.toString(); // stack trace as a string
                }
                return "" + o;
            }
        };
        public SysOutDelegate(Object arg) throws Exception {
            super(arg);
            name = getName(arg);
        }

        private boolean log(String level, Object ... args){
            System.out.println( level + "| " + name + ": " + StringUtil.join(args, " ", joinIterator));
            return true;
        }

        @Override
        public boolean error(Object... args) {
            return log("E", args);
        }

        @Override
        public boolean info(Object... args) {
            return log("I", args);
        }

        @Override
        public boolean trace(Object... args) {
            return log("T", args);
        }

        @Override
        public boolean fatal(Object... args) {
            return log("F", args);
        }

        @Override
        public boolean warn(Object... args) {
            return log(" W", args);
        }

        @Override
        public boolean debug(Object... args) {
            return log("D", args);
        }

        @Override
        public boolean log(Object... args) {
            return log("L", args);
        }

        @Override
        public boolean isTraceEnabled() {
            return true;
        }

        @Override
        public boolean isInfoEnabled() {
            return true;
        }

        @Override
        public boolean isDebugEnabled() {
            return true;
        }
    }


    private static class AndroidLogDelegate extends Delegate {
        private String tag;

        public AndroidLogDelegate(Object arg) throws Exception {
            super(arg);

            if (arg instanceof Class){
               tag = ((Class) arg).getCanonicalName();
            } else {
                tag = getName(arg);
            }


        }


        private boolean log(String level, Object ... args){
            try {
                if (args.length == 1) {
                    forName(ANDROID_LOG1_CLASS)
                            .getDeclaredMethod(level, String.class, String.class).invoke(null, tag, valueOf(args[0]));
                }
                else if (args.length == 2 && args[1] instanceof Throwable) {
                    forName(ANDROID_LOG1_CLASS)
                            .getDeclaredMethod(level, String.class, String.class, Throwable.class)
                            .invoke(null, tag, valueOf(args[0]), args[1]);
                }
                else {
                    forName(ANDROID_LOG1_CLASS)
                            .getDeclaredMethod(level, String.class, String.class).invoke(null, tag, StringUtil.join(args, " "));
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return true;
        }


        private boolean isLoggable(int level){
            try {
                return (boolean) forName(ANDROID_LOG1_CLASS)
                        .getDeclaredMethod("isLoggable").invoke(null, tag, level);
            } catch (Exception e) {
                return false;
            }
        }





        @Override
        public boolean error(Object... args) {
            return log("e", args);
        }

        @Override
        public boolean info(Object... args) {
            return log("i", args);
        }

        @Override
        public boolean trace(Object... args) {
            return log("v", args);
        }

        @Override
        public boolean fatal(Object... args) {
            return log("wtf", args);
        }

        @Override
        public boolean warn(Object... args) {
            return log("w", args);
        }

        @Override
        public boolean debug(Object... args) {
            return log("d", args);
        }

        @Override
        public boolean log(Object... args) {
            return log("i", args);
        }

        /**
         *     public static final int ASSERT = 7;
         *     public static final int DEBUG = 3;
         *     public static final int ERROR = 6;
         *     public static final int INFO = 4;
         *     public static final int VERBOSE = 2;
         *     public static final int WARN = 5;
         * @return
         */
        @Override
        public boolean isTraceEnabled() {
            return isLoggable(2); //VERBOSE = 2;
        }

        @Override
        public boolean isInfoEnabled() {
            return isLoggable(4); //INFO = 4;
        }

        @Override
        public boolean isDebugEnabled() {
            return isLoggable(3); //DEBUG = 3;
        }
    }
}
