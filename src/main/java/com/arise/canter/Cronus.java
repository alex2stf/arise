package com.arise.canter;

import com.arise.core.AppSettings;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.MapUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.ThreadUtil;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Cronus {

    private final List<CronTask> cronTasks = new ArrayList<>();
    private Registry registry;

    public Cronus(Registry registry){
        this.registry = registry;

        boolean disabled = AppSettings.isFalse(AppSettings.Keys.CRONUS_ENABLED);
        if (disabled){
            return;
        }
        String path = AppSettings.getProperty(AppSettings.Keys.CRONUS_CONFIG_FILE, "resources#weland/config/cronus.json");


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
                }
            }
        }, 1000);
    }


    private void registerJob(Map m){
        String hour = MapUtil.getString(m, "hour");
        String name = MapUtil.getString(m, "name");
        String day = MapUtil.getString(m, "day");
        boolean disable = MapUtil.getBool(m, "disable");
        Map cmd = MapUtil.getMap(m, "cmd");
        String cmdId = MapUtil.getString(cmd, "id");
        String args[] = MapUtil.getStringList(cmd, "args");
        cronTasks.add(
               new CronTask(registry)
                       .setDisable(disable)
                       .setDayRef(day)
                       .setName(name)
                       .setHourRef(hour)
                       .setCmdId(cmdId)
                       .setArgs(args)
        );
    }



    private class CronTask {
        String dayRef;
        String hourRef;
        String name;
        String cmdId;
        String args[];
        private Registry registry;
        private boolean disable = false;

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
            System.out.println(moment +  " ==  " + sdf.format(date));
            return moment.equalsIgnoreCase(sdf.format(date));
        }

        String getHourRef(){
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            if ("EACH_SECOND".equalsIgnoreCase(hourRef)){
                return sdf.format(new Date());
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
            ThreadUtil.fireAndForget(new Runnable() {
                @Override
                public void run() {
                    registry.execute(cmdId, args, null, null);
                }
            }, "cronus-task-" + UUID.randomUUID().toString());

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
