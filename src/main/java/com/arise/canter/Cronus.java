package com.arise.canter;

import com.arise.core.exceptions.SyntaxException;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.MapUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.ThreadUtil;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
            return parseHourRef(hourRef, Calendar.getInstance());
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


    private static final String nilh = "xx:xx:xx";

    public static String parseHourRef(String input, Calendar calendar){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String parts[] = input.split(" ");

        String moment = parts[0].toLowerCase();
        String part2 = parts.length > 1 ? parts[1].toLowerCase() : null;

        //between syntax
        if (null != part2){
            if (part2.startsWith("between_")){
                String substr = part2.substring("between_".length());
                parts = substr.split("_and_");
                if (!isBetween(parts[0],calendar, parts[1])){
                    return nilh;
                }
            }
        }

        if ("each_second".equalsIgnoreCase(moment)){
            return sdf.format(calendar.getTime());
        }
        if ("each_minute".equalsIgnoreCase(moment)){
            if(calendar.get(Calendar.SECOND) == 01) {
                return sdf.format(calendar.getTime());
            }
            return "xx:xx:xx";
        }
        if (moment.startsWith("each_") && moment.endsWith("_seconds")){
            String sec = moment.split("_")[1];
            try {
                int num = Integer.valueOf(sec);
                int seconds = calendar.get(Calendar.SECOND);
                if (seconds % num == 0){
                    return sdf.format(calendar.getTime());
                }
                return "xx:xx:xx";
            } catch (Exception e){
                throw new SyntaxException("invalid hourRef " + input, e);
            }
        }
        if (moment.startsWith("each_") && moment.endsWith("_minutes")){
            String sec = moment.split("_")[1];
            try {
                int num = Integer.valueOf(sec);
                int minute = calendar.get(Calendar.MINUTE);
                if (minute % num == 0 && 59 == calendar.get(Calendar.SECOND)){
                    return sdf.format(calendar.getTime());
                }
                return "xx:xx:xx";
            } catch (Exception e){
                throw new SyntaxException("invalid hourRef " + input, e);
            }
        }
        try {
            Date d = sdf.parse(moment);
            return sdf.format(d);
        } catch (ParseException e) {
            throw new SyntaxException("Invalid input " + input, e);
        }
    }


    static MomentInDay fromString(String s){
        String parts[] = s.split(":");
        MomentInDay m = new MomentInDay();
        try {
           m._h = Integer.parseInt(parts[0]);
           m._m = Integer.parseInt(parts[1]);
           m._s = Integer.parseInt(parts[2]);
        } catch (Exception e){
           throw new SyntaxException("Invalid string " + s + " for parsing MomentInDay");
        }
        return m;
    }

    public static boolean isBetween(String x, String s, String y){
        return isBetween(fromString(x), fromString(s), fromString(y));
    }

    public static boolean isBetween(String x, Calendar s, String y){
        MomentInDay m = new MomentInDay();
        m._h = s.get(Calendar.HOUR_OF_DAY);
        m._m = s.get(Calendar.MINUTE);
        m._s = s.get(Calendar.SECOND);
        return isBetween(fromString(x), m, fromString(y));
    }


    public static boolean isBefore(String a, String m){
        return isBefore(fromString(a), fromString(m));
    }

    public static boolean isBetween(MomentInDay i, MomentInDay m, MomentInDay d){
        return isAfter(m, d) && isBefore(i, m);
    }

    public static boolean isBefore(MomentInDay a, MomentInDay m){
        if (m.hour() == a.hour()){
            if (m.minute() == a.minute()){
                return m.second() > a.second();
            }
            return m.minute() > a.minute();
        }
        return m.hour() > a.hour();
    }

    public static boolean isAfter(MomentInDay a, MomentInDay m){
        return isBefore(a, m);
    }

    public static boolean isAfter(String a, String m){
        return isAfter(fromString(a), fromString(m));
    }




    public static class MomentInDay {
        int _h;
        int _m;
        int _s;

        public int hour() {
            return _h;
        }

        public int minute() {
            return _m;
        }

        public int second() {
            return _s;
        }
    }

}
