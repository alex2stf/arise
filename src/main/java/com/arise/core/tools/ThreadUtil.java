package com.arise.core.tools;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class ThreadUtil {

    public static Thread fireAndForget(Runnable action, String name){
        Thread t = new Thread(action);
        if (name != null){
            t.setName(name);
        }
        t.start();
        return t;
    }

    public static Thread fireAndForget(Runnable action){
        return fireAndForget(action, "FireAndForget" + UUID.randomUUID().toString() + "-" + System.currentTimeMillis());
    }

    public static TimerResult repeatedTask(Runnable action, long rate){
        Timer timer = new Timer(true);
        TimerResult timerResult = new TimerResult(timer, action);
        timer.scheduleAtFixedRate(timerResult.getTimerTask(), 0, rate);
        return timerResult;
    }

    public static TimerResult delayedTask(Runnable action, long delay){
        Timer timer = new Timer(true);
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
