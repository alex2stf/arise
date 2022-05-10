package com.arise.core.tools;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class ThreadUtil {


    public static Thread startDaemon(Runnable action, String name){
        return fireAndForget(action, name, true);
    }


    public static Thread fireAndForget(Runnable action, String name){
        return fireAndForget(action, name, false);
    }

    public static Thread fireAndForget(final Runnable action, String name, boolean daemon){

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                action.run();
                try {
                    Thread.currentThread().interrupt();
                }catch (Exception e){
                    ;;
                }
            }
        });
        t.setDaemon(daemon); //WTF is this?
        if (name != null){
            t.setName(name);
        }
        try {
            t.start();
        }catch (OutOfMemoryError error){
            error.printStackTrace();
            t.run();
        }

        return t;
    }

    @Deprecated
    public static Thread fireAndForget(Runnable action){
        return fireAndForget(action, "FireAndForget" + UUID.randomUUID().toString() + "-" + System.currentTimeMillis());
    }

    public static TimerResult repeatedTask(Runnable action, long rate){
        Timer timer = new Timer(false);
        TimerResult timerResult = new TimerResult(timer, action);
        timer.scheduleAtFixedRate(timerResult.getTimerTask(), 0, rate);
        return timerResult;
    }

    public static TimerResult delayedTask(Runnable action, long delay){
        Timer timer = new Timer(false);
        TimerResult timerResult = new TimerResult(timer, action);
        timer.schedule(timerResult.getTimerTask(), delay);
        return timerResult;
    }

    public static void closeTimer(TimerResult result){
        if (result == null){
            return;
        }
        result.canRun = false;
        result.timerTask.cancel();
        result.timer.cancel();
        result.timer.purge();
    }

    public static String threadId(String id) {
        return id.replaceAll("\\s+", "-") + "-" + UUID.randomUUID();
    }


    public  static class TimerResult {
        final Timer timer;
        final TimerTask timerTask;
        volatile boolean canRun = true;

        public TimerResult(Timer timer, final Runnable runnable) {
            this.timer = timer;
            this.timerTask = new TimerTask() {
                @Override
                public void run() {
                    if (canRun){
                        runnable.run();
                    }else {
                        closeTimer(self());
                    }
                }
            };
        }

        TimerResult self(){
            return this;
        }

        public TimerTask getTimerTask() {
            return timerTask;
        }
    }







}
