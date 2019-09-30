package com.arise.core.tools;


import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;


/**
 * Created by alex on 27/11/2017.
 */
public class Mole {



    private static final Map<Class<? extends Delegate>, ValidityCondition> delegates = new ConcurrentHashMap<>();

    private static final String ANDROID_LOG1_CLASS = "android.util.Log";
    private static final String APACHE_LOG4J_CLASS = "org.apache.log4j.Logger";

    public static <T extends Delegate> void registerDelegate(Class<T> tClass, ValidityCondition<T> condition){
        if (!delegates.containsKey(tClass)){
            delegates.put(tClass, condition);
        }
    }

    static {
        registerDelegate(AndroidLogDelegate.class, new ValidityCondition<AndroidLogDelegate>() {
            @Override
            public boolean isValid(Class<AndroidLogDelegate> delegateClass) {
                return ReflectUtil.classExists(ANDROID_LOG1_CLASS);
            }
        });

        registerDelegate(Log4JDelegate.class, new ValidityCondition<Log4JDelegate>() {
            @Override
            public boolean isValid(Class<Log4JDelegate> delegateClass) {
                return ReflectUtil.classExists(APACHE_LOG4J_CLASS);
            }
        });

        registerDelegate(JUtilDelegate.class, new ValidityCondition<JUtilDelegate>() {
            @Override
            public boolean isValid(Class<JUtilDelegate> delegateClass) {
                return false;
            }
        });

        registerDelegate(SysOutDelegate.class, new ValidityCondition<SysOutDelegate>() {
            @Override
            public boolean isValid(Class<SysOutDelegate> delegateClass) {
                return true;
            }
        });
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

    public Mole(String name){
        scanDelegates(name);

    }
    public Mole(Class clazz){
        scanDelegates(clazz);
    }


    private void scanDelegates(Object arg) {
        for (Map.Entry<Class<? extends Delegate>, ValidityCondition> entry: delegates.entrySet()){
            if (entry.getValue().isValid(entry.getKey())){
                try {
                    delegate = entry.getKey().getDeclaredConstructor(Object.class).newInstance(arg);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
//                System.out.println(String.valueOf(arg) + " Setup delegate " + entry.getKey());
                break;
            }
        }
    }



    public void log(Object ... args){
        delegate.log(args);
    }

    public void trace(Object ... args) {
        delegate.trace(args);
    }


    public void debug(Object ... args) {
        delegate.debug(args);
    }


    public void error(Object ... args) {
        delegate.error(args);
    }


    public void info(Object ... args) {
        delegate.info(args);
    }

    public void warn(Object ... args) {
        delegate.warn(args);
    }

    public void fatal(Object ... args) {
        delegate.fatal(args);
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


    private interface ValidityCondition<T extends Delegate>{
        boolean isValid(Class<T> delegateClass);
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
            logger = java.util.logging.Logger.getLogger(String.valueOf(arg));
        }


        private boolean callLevel(java.util.logging.Level level, Object ... args){
            if (args == null){
                return true;
            }
            if (args.length == 1){
                if (args[0] instanceof Throwable){
                    logger.log(level, StringUtil.dump(args[0]));
                } else {
                    logger.log(level, String.valueOf(args[0]));
                }

            } else if (args.length == 2 && args[1] instanceof Throwable){
                logger.log(level, String.valueOf(args[0]), (Throwable) args[1]);
            } else {
                logger.log(level, StringUtil.joinFormat(args));
            }
            return true;

        }

        @Override
        public boolean error(Object... args) {
            return callLevel(java.util.logging.Level.SEVERE, args);
        }

        @Override
        public boolean info(Object... args) {
            return callLevel(java.util.logging.Level.FINER, args);
        }

        @Override
        public boolean trace(Object... args) {
            return callLevel(java.util.logging.Level.FINEST, args);
        }

        @Override
        public boolean fatal(Object... args) {
            return callLevel(java.util.logging.Level.SEVERE, args);
        }

        @Override
        public boolean warn(Object... args) {
            return callLevel(java.util.logging.Level.WARNING, args);
        }

        @Override
        public boolean debug(Object... args) {
            return callLevel(java.util.logging.Level.FINE, args);
        }

        @Override
        public boolean log(Object... args) {
            if (args.length > 1 && args[0] instanceof java.util.logging.Level){

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
            clzz =  Class.forName(clzName);
            if (clzz == null){
                throw new RuntimeException("failed to init");
            }
            if (arg instanceof String){
                log4J = clzz.getDeclaredMethod("getLogger", String.class).invoke(null, arg);
            } else if (arg instanceof Class){
                log4J = clzz.getDeclaredMethod("getLogger", Class.class).invoke(null, arg);
            }

            if (log4J == null){
                throw new RuntimeException("failed to init");
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
                    clzz.getMethod(method, Object.class).invoke(log4J, StringUtil.joinFormat(",", args));
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
            return String.valueOf(arg);
        }
    }


    private static class SysOutDelegate extends Delegate {

        private String name;
        public SysOutDelegate(Object arg) throws Exception {
            super(arg);
            name = getName(arg);
        }

        private boolean log(String level, Object ... args){
            System.out.println( level + "| " + name + ": " + StringUtil.joinFormat(args));
            return true;
        }

        @Override
        public boolean error(Object... args) {
            return log("ERROR", args);
        }

        @Override
        public boolean info(Object... args) {
            return log(" INFO", args);
        }

        @Override
        public boolean trace(Object... args) {
            return log("TRACE", args);
        }

        @Override
        public boolean fatal(Object... args) {
            return log("FATAL", args);
        }

        @Override
        public boolean warn(Object... args) {
            return log(" WARN", args);
        }

        @Override
        public boolean debug(Object... args) {
            return log("DEBUG", args);
        }

        @Override
        public boolean log(Object... args) {
            return log("  LOG", args);
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
                    Class.forName(ANDROID_LOG1_CLASS)
                            .getDeclaredMethod(level, String.class, String.class).invoke(null, tag, String.valueOf(args[0]));
                }
                else if (args.length == 2 && args[1] instanceof Throwable) {
                    Class.forName(ANDROID_LOG1_CLASS)
                            .getDeclaredMethod(level, String.class, String.class, Throwable.class)
                            .invoke(null, tag, String.valueOf(args[0]), args[1]);
                }
                else {
                    Class.forName(ANDROID_LOG1_CLASS)
                            .getDeclaredMethod(level, String.class, String.class).invoke(null, tag, StringUtil.joinFormat(args));
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
                return (boolean) Class.forName(ANDROID_LOG1_CLASS)
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
