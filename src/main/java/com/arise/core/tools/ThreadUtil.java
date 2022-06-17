package com.arise.core.tools;

import java.util.*;

public class ThreadUtil {

    @Deprecated
    public static Thread fireAndForget(Runnable action, String name){
        return startNewThread(action, "FF-" + name, false, -1);
    }

    public static Thread startDaemon(Runnable action, String name){
        return startNewThread(action, "DU-" + name, true, -1);
    }


    public static Thread startThread(Runnable action, String name){
        return startNewThread(action, "UT-" + name, false, -1);
    }

    public static Thread startJoinedThread(Runnable a, String n){
        return startNewThread(a, "JT-" + n, false, 0);
    }

    public static Thread startJoinedDaemon(Runnable a, String n){
        return startNewThread(a, "JD-" + n, true, 0);
    }




    private static Thread startNewThread(final Runnable action,
                                         String name,
                                         boolean daemon,
                                         int join){

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                action.run();
                try {
                    Thread.currentThread().interrupt();
                } catch (Exception e){
                    ;;
                }
            }
        });
        t.setDaemon(daemon);
        if (name != null){
            t.setName(threadId(name));
        }
        try {
            t.start();
            if (join > -1){
                try {
                    t.join(join);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (OutOfMemoryError error){
            error.printStackTrace();
            t.run();
        }

        return t;
    }

    public static TimerResult repeatedTask(Runnable action, long rate){
        Timer timer = new Timer("Repeated-Task-" + UUID.randomUUID() , false);
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

    private static String threadId(String id) {
        return id.replaceAll("\\s+", "-") + "-" + UUID.randomUUID();
    }

    public static void sleep(long v) {
        try {
            Thread.sleep(v);
        } catch (InterruptedException e) {
            Mole.getInstance(ThreadUtil.class).e("sleep interrupted ", e);
        }
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
