package com.arise.canter;

import com.arise.core.AppSettings;
import com.arise.core.exceptions.SyntaxException;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.MapUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.ThreadUtil;
import com.arise.weland.ui.WelandForm;

import java.io.IOException;
import java.rmi.MarshalledObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.arise.core.tools.ThreadUtil.fireAndForget;
import static com.arise.core.tools.ThreadUtil.threadId;

public class Cronus {

    private final List<CronTask> cronTasks = new ArrayList<>();

    private Registry registry;
    private List<Runnable> otherTasks = new ArrayList<>();


    public Cronus(Registry registry, String path){
        this.registry = registry;


        try {
            List config = (List) Groot.decodeBytes(
                    StreamUtil.toBytes(FileUtil.findStream(path))
            );
            for (Object o: config){
                if (o instanceof Map){
                    Map m = (Map) o;
                    registerJob(m);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        ThreadUtil.repeatedTask(new Runnable() {
            @Override
            public void run() {
                for (CronTask c: cronTasks){
                    if (c.isMatching(new Date())){
                        c.execute();
                    }
                    for (Runnable x: otherTasks){
                        fireAndForget(x, threadId("other-task"));
                    }
                }
            }
        }, 1000);
    }






    private void registerJob(Map m){
        String hour = MapUtil.getString(m, "hour");
        String name = MapUtil.getString(m, "name");
        String day = MapUtil.getString(m, "day");
        boolean disable = MapUtil.getBool(m, "disable");
        String storeKey = MapUtil.getString(m, "store-key");
        Map cmd = MapUtil.getMap(m, "cmd");
        String cmdId = MapUtil.getString(cmd, "id");
        String args[] = MapUtil.getStringList(cmd, "args");
        cronTasks.add(
               new CronTask(registry)
                       .setDisable(disable)
                       .setDayRef(day)
                       .setStoreKey(storeKey)
                       .setName(name)
                       .setHourRef(hour)
                       .setCmdId(cmdId)
                       .setArgs(args)
        );
    }

    public void registerTask(Runnable task) {
        otherTasks.add(task);
    }


    private class CronTask {
        String dayRef;
        String hourRef;
        String name;
        String cmdId;
        String storeKey;
        String args[];
        private Registry registry;
        private boolean disable = false;

        public CronTask setStoreKey(String storeKey){
            this.storeKey = storeKey;
            return this;
        }
        public CronTask(Registry registry) {
            this.registry = registry;
        }

        CronTask setDayRef(String dayRef){
            this.dayRef = dayRef;
            return this;
        }


        boolean isMatching(Date date){
            if (disable){
                return false;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String moment = getDayRef() + " " + getHourRef();
//            System.out.println("compare " + moment + " with " + sdf.format(date));
            return moment.equalsIgnoreCase(sdf.format(date));
        }

        String getHourRef(){
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            Calendar calendar = Calendar.getInstance();
            if ("EACH_SECOND".equalsIgnoreCase(hourRef)){
                return sdf.format(calendar.getTime());
            }
            if ("EACH_MINUTE".equalsIgnoreCase(hourRef)){
                if(calendar.get(Calendar.SECOND) == 59) {
                    return sdf.format(calendar.getTime());
                }
                return "xx:xx:xx";
            }
            if (hourRef.startsWith("EACH_") && hourRef.endsWith("_SECONDS")){
                String sec = hourRef.split("_")[1];
                try {
                  int num = Integer.valueOf(sec);
                  int seconds = calendar.get(Calendar.SECOND);
                  if (seconds % num == 0){
                      return sdf.format(calendar.getTime());
                  }
                  return "xx:xx:xx";
                } catch (Exception e){
                    throw new SyntaxException("invalid hourRef " + hourRef, e);
                }
            }
            if (hourRef.startsWith("EACH_") && hourRef.endsWith("_MINUTES")){
                String sec = hourRef.split("_")[1];
                try {
                    int num = Integer.valueOf(sec);
                    int minute = calendar.get(Calendar.MINUTE);
                    if (minute % num == 0 && 59 == calendar.get(Calendar.SECOND)){
                        return sdf.format(calendar.getTime());
                    }
                    return "xx:xx:xx";
                } catch (Exception e){
                    throw new SyntaxException("invalid hourRef " + hourRef, e);
                }
            }
            try {
                Date d = sdf.parse(hourRef);
                return sdf.format(d);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

        String getDayRef(){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if ("DAILY".equalsIgnoreCase(dayRef)){
                return sdf.format(new Date());
            }
            try {
                Date d = sdf.parse(dayRef);
                return sdf.format(d);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void execute() {
            fireAndForget(new Runnable() {
                @Override
                public void run() {
                   Object res = registry.execute(cmdId, args, null, null);
                    if(StringUtil.hasText(storeKey)){
                        registry.store(storeKey, res);
                    }
                }
            }, "cronus-task-" + UUID.randomUUID(), true);

        }

        private CronTask setCmdId(String cmdId) {
            this.cmdId = cmdId;
            return this;
        }

        public CronTask setName(String name) {
            this.name = name;
            return this;
        }

        public CronTask setHourRef(String hourRef) {
            this.hourRef = hourRef;
            return this;
        }

        public CronTask setArgs(String[] args) {
            this.args = args;
            return this;
        }

        public CronTask setDisable(boolean disable) {
            this.disable = disable;
            return this;
        }
    }

    public static void main(String[] args) {
        Mole.getInstance(Cronus.class).log("Cronus instance started standalone at " + new Date());
    }



}
