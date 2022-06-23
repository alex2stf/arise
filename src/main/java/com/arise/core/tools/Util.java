package com.arise.core.tools;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.sql.Connection;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class Util {


    private static final Set<Object> globalContexts = new HashSet<>();

    private Util(){

    }

    public static void registerContext(Object context){
        globalContexts.add(context);
    }

    public static  Set<Object> getGlobalContexts(){
        return Collections.unmodifiableSet(globalContexts);
    }



    public static Object getContext(String clazz){
        for (Object o: globalContexts){
            if (o != null & ReflectUtil.objectIsAssignableFrom(o, clazz)){
                return o;
            }
        }
        return null;
    }


    public static void close(Object stream){
        if (stream == null){
            return;
        }
        if(stream instanceof ThreadUtil.TimerResult){
            ThreadUtil.closeTimer((ThreadUtil.TimerResult)stream);
            return;
        }
        try {
            closeWithEx(stream);
        } catch (Exception e){
            ;;
        }
    }


    public static int randBetween(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    public static void rejectNullArgs(Object... args) {
        for (Object arg : args) {
            rejectNull(arg);
        }
    }

    public static void rejectNull(Object arg) {
        if (arg == null) {
            throw new NullPointerException("Null argument rejected");
        }
    }


    private static void closeWithEx(Object stream) throws Exception {
        if (stream instanceof Thread){
            try {
                ((Thread)stream).interrupt();
            } catch (Exception e){

            }
        }

        if (stream instanceof Closeable){
            ((Closeable) stream).close();
        }

        if (stream instanceof AutoCloseable){
            ((AutoCloseable) stream).close();
        }

        if (stream instanceof Reader) {
            ((Reader)stream).close();
        }
        else if (stream instanceof Writer) {
            ((Writer)stream).close();
        }
        else if (stream instanceof InputStream) {
            ((InputStream)stream).close();
        }
        else if (stream instanceof OutputStream) {
            ((OutputStream)stream).close();
        }
        else if (stream instanceof Socket) {
            ((Socket)stream).close();
        }
        else if (stream instanceof ServerSocket) {
            ((ServerSocket)stream).close();
        }
        else if (stream instanceof SocketChannel) {
            ((SocketChannel)stream).close();
        }
        else if (stream instanceof Selector) {
            ((Selector)stream).close();
        }

        else if (stream instanceof Connection) {
            ((Connection)stream).close();
        }

        else if (stream instanceof Statement) {
            ((Statement)stream).close();
        }

        else if (stream instanceof Closeable) {
            ((Closeable)stream).close();
        }

        else {
            ReflectUtil.getMethod(stream, "close").call();
        }

    }




    public static int mathMin(int[] values){
        return MathUtil.min(values);
    }


    public static int mathMax(int[] values){
        return MathUtil.max(values);
    }

    public static Object coerce(Object object) {
        if (ReflectUtil.objectIsAssignableFrom(object, "java.util.Optional")) {
            if (ReflectUtil.getMethod(object, "isPresent").callForBoolean()) {
                return coerce(ReflectUtil.getMethod(object, "get").callFor(Object.class));
            } else {
                return null;
            }
        }
        return object;
    }


    /**
     *
     * <code>
     *     final Handler handler = new Handler();
     *     handler.postDelayed(new Runnable() {
     *          //Do something after 100ms
     *     }, 100);
     * </code>
     */
    @Deprecated
    public static final class ThreadFactory {
        private static final Executor fixedThreadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        public static void put(Runnable runnable){
           fixedThreadPool.execute(runnable);
        }

        public static void fireAndForget(Runnable runnable){
            new Thread(runnable).start();
        }


        public static void runLater(Runnable runnable, long delayMillis){
            if (ReflectUtil.classExists("android.os.Handler")){
                try {
                    Object _handler = ReflectUtil.newInstance("android.os.Handler");
                    ReflectUtil.getMethod(_handler, "postDelayed", Runnable.class, long.class)
                            .call(runnable, delayMillis);
                } catch (Throwable e){

                }
                return;
            }
            throw new RuntimeException("TODO");
        }

        public static void asyncTask(Runnable runnable) {
            fireAndForget(runnable);
        }
    }


    




}
