package com.arise.canter;

import com.arise.core.exceptions.LogicalException;
import com.arise.core.exceptions.SyntaxException;
import com.arise.core.serializers.parser.Groot;
import com.arise.core.tools.FileUtil;
import com.arise.core.tools.MapUtil;
import com.arise.core.tools.Mole;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.StringUtil;
import com.arise.core.tools.Util;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.arise.core.tools.ThreadUtil.repeatedTask;
import static com.arise.core.tools.Util.EXTFMT;

public class Cronus {

    private final List<CronTask> cronTasks = new ArrayList<>();

    private CommandRegistry commandRegistry;
    private List<Runnable> otherTasks = new ArrayList<>();


    private static final Map<String, Integer> wk = Collections.unmodifiableMap(new HashMap<String, Integer>() {{
        put("monday", 1);
        put("tuesday", 2);
        put("wednesday", 3);
        put("thursday", 4);
        put("friday", 5);
        put("saturday", 6);
        put("sunday", 7);
    }});


    private static final SimpleDateFormat WKDAYFMT = new SimpleDateFormat("EEEE", Locale.US);
    private static final SimpleDateFormat MNTHFMT = new SimpleDateFormat("MMMM", Locale.US);
    public static final String nilh = "xx:xx:xx";
    private static final SimpleDateFormat STDFMT = new SimpleDateFormat("yyyy-MM-dd");

    public Cronus(CommandRegistry commandRegistry, String path) {
        this.commandRegistry = commandRegistry;


        try {
            List config = (List) Groot.decodeBytes(
                    StreamUtil.toBytes(FileUtil.findStream(path))
            );
            for (Object o : config) {
                if (o instanceof Map) {
                    Map m = (Map) o;
                    registerJob(m);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        repeatedTask(new Runnable() {
            @Override
            public void run() {
                for (final CronTask c : cronTasks) {
                    if (c.isMatching(Calendar.getInstance())) {
                        c.execute();
                    }
                    for (Runnable x : otherTasks) {
                        x.run();
                    }
                }
            }
        }, 1000);
    }

    public static MomentInDay fromCalendar(Calendar c) {
        MomentInDay m = new MomentInDay();
        m._h = c.get(Calendar.HOUR_OF_DAY);
        m._m = c.get(Calendar.MINUTE);
        m._s = c.get(Calendar.SECOND);
        return m;
    }

    public static Calendar decorate(MomentInDay m, Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, m._h);
        c.set(Calendar.MINUTE, m._m);
        c.set(Calendar.SECOND, m._s);
        return c;
    }

    public static String strfNowPlusMillis(long t) {
        Date d = Util.now();
        d.setTime(d.getTime() + t);
        return strf(d);
    }

    //TODO use more
    public static String strf(Date d){
        return EXTFMT.format(d);
    }

    public static String strNow(){
        return EXTFMT.format(Util.now());
    }

    public static String strfMillis(double t) {
        if (t < 1000){
            return t + " miliseconds";
        }

        if (t >= 1000 && t <= 1000 * 60){
            double d = t / 1000d;
            if (Double.compare(d, 1d) == 0){
                return "1 second";
            }
            return d + " seconds";
        }

        if (t > 1000 * 60 && t <= 1000 * 60 * 60){
            return ((t / 1000) / 60) + " minutes";
        }

        if (t > 1000 * 60 * 60 && t <= 1000 * 60 * 60 * 24){
            return ((t / 1000) / 60 / 60) + " hours";
        }

        return ((t / 1000) / 60 / 60 / 24) + " days";
    }


    private void registerJob(Map m) {
        String hour = MapUtil.getString(m, "hour");
        String name = MapUtil.getString(m, "name");
        String day = MapUtil.getString(m, "day");
        boolean disable = MapUtil.getBool(m, "disable");
        String storeKey = MapUtil.getString(m, "store-key");
        Map cmd = MapUtil.getMap(m, "cmd");
        String cmdId = MapUtil.getString(cmd, "id");
        String args[] = MapUtil.getStringList(cmd, "args");
        cronTasks.add(
                new CronTask(commandRegistry)
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
        private CommandRegistry cmdReg;
        private boolean disable = false;

        public CronTask setStoreKey(String storeKey) {
            this.storeKey = storeKey;
            return this;
        }

        public CronTask(CommandRegistry cmdReg) {
            this.cmdReg = cmdReg;
        }

        CronTask setDayRef(String dayRef) {
            this.dayRef = dayRef;
            return this;
        }


        boolean isMatching(Calendar c) {
            if (disable) {
                return false;
            }
            return matchMoment(c, dayRef, hourRef);
        }

        public void execute() {
            Object res = cmdReg.execute(cmdId, args, null, null);
            if (StringUtil.hasText(storeKey)) {
                cmdReg.store(storeKey, res);
            }
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
        Mole.getInstance(Cronus.class).log("Cronus instance started standalone at " + Util.now());
    }


    public static boolean matchMoment(Calendar c, String d, String h) {
        String moment = parseDayRef(d, c) + " " + parseHourRef(h, c);
        String oth = EXTFMT.format(c.getTime());
     //   Mole.getInstance(">>>>>>>>  ").info(h + "========> "  + moment + " ==  " + oth);
        return moment.equalsIgnoreCase(oth);
    }




    public static Map<Integer, List<String>> getParts(String x) {
        int i = x.indexOf(" ");
        Map<Integer, List<String>> res = new HashMap<>();
        if(i < 0){
            res.put(1, Arrays.asList(x));
            return res;
        }
        String a = x.substring(0, i);
        String p2 = x.substring(i + 1);
        String b = null;
        String c = null;

        if (p2.indexOf(" ") > -1){
            i = p2.indexOf(" ");
            b = p2.substring(0, i).toLowerCase();
            c = p2.substring(i + 1).toLowerCase();
        } else {
            b = p2.toLowerCase();
        }

        res.put(1, Arrays.asList(a));

        if (null != b && b.startsWith("between_")) {
            String substr = b.substring("between_".length());
            String p[] = substr.split("_and_");
            res.put(2, Arrays.asList(p[0], p[1]));
        }

        if (null != b && b.startsWith("except:")) {
            String d = b.substring("except:".length());
            res.put(3, Arrays.asList(d));
            return res;
        }

        if (null != c && c.startsWith("except:")){
            String d = c.substring("except:".length());
            res.put(3, Arrays.asList(d));
        }
        return res;
    }

    public static String parseDayRef(String in, Calendar c) {

        Map<Integer, List<String>> parts = getParts(in);
        String moment = parts.get(1).get(0).toLowerCase();

        if (parts.containsKey(2)) {   //between
            String fromDay = parts.get(2).get(0);
            String toDay = parts.get(2).get(1);
            if (!dayIsBetween(fromDay, c, toDay)) {
                return nilh;
            }
        }

        if (parts.containsKey(3)){  //except
            String except = parts.get(3).get(0);
            if (isException(except, c)){
                return nilh;
            }
        }


        if ("DAILY".equalsIgnoreCase(moment)) {
            return STDFMT.format(c.getTime());
        }

        if (moment.indexOf("_") > -1 || isWeekdayFormat(moment)){
            String dwd[] = moment.toLowerCase().split("_");
            if (isWeekdayFormat(dwd)){
                Date d = c.getTime();
                String n = WKDAYFMT.format(d).toLowerCase();
                for (String s: dwd){
                    if (n.equals(s)){
                        return STDFMT.format(d);
                    }
                }
                return nilh;
            }
            return nilh;
        }
        try {
            Date d = STDFMT.parse(moment);
            return STDFMT.format(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }



    private static boolean isException(String s, Calendar c) {
        String p[] = s.split(",");
        String w = WKDAYFMT.format(c.getTime()).toLowerCase();
        String m = MNTHFMT.format(c.getTime()).toLowerCase();

        for (String x: p){
            if (w.equalsIgnoreCase(x) || m.equalsIgnoreCase(x)){
                return true;
            }
        }
        return false;
    }


    public static String parseHourRef(String input, Calendar calendar) {
        Map<Integer, List<String>> parts = getParts(input);
        String moment = parts.get(1).get(0).toLowerCase();
        if (parts.containsKey(2)){
            String from = parts.get(2).get(0);
            String to = parts.get(2).get(1);
            if (!isBetween(from, calendar, to)){
                return nilh;
            }
        }
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.US);
        if ("each_second".equalsIgnoreCase(moment)) {
            return sdf.format(calendar.getTime());
        }
        if ("each_minute".equalsIgnoreCase(moment)) {
            if (calendar.get(Calendar.SECOND) == 01) {
                return sdf.format(calendar.getTime());
            }
            return nilh;
        }
        if (moment.startsWith("each_") && moment.endsWith("_seconds")) {
            String sec = moment.split("_")[1];
            try {
                int num = Integer.valueOf(sec);
                int seconds = calendar.get(Calendar.SECOND);
                if (seconds % num == 0) {
                    return sdf.format(calendar.getTime());
                }
                return nilh;
            } catch (Exception e) {
                throw new SyntaxException("invalid hourRef " + input, e);
            }
        }
        if (moment.startsWith("each_") && moment.endsWith("_minutes")) {
            String sec = moment.split("_")[1];
            try {
                int num = Integer.valueOf(sec);
                int minute = calendar.get(Calendar.MINUTE);
                if (minute % num == 0 && 59 == calendar.get(Calendar.SECOND)) {
                    return sdf.format(calendar.getTime());
                }
                return nilh;
            } catch (Exception e) {
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


    public static MomentInDay fromString(String s) {
        String parts[] = s.split(":");
        MomentInDay m = new MomentInDay();
        try {
            m._h = Integer.parseInt(parts[0]);
            m._m = Integer.parseInt(parts[1]);
            m._s = Integer.parseInt(parts[2]);
        } catch (Exception e) {
            throw new SyntaxException("Invalid string " + s + " for parsing MomentInDay");
        }
        return m;
    }

    public static boolean isBetween(String x, String s, String y) {
        return isBetween(fromString(x), fromString(s), fromString(y));
    }

    public static boolean isBetween(String x, Calendar s, String y) {
        MomentInDay m = fromCalendar(s);
        return isBetween(fromString(x), m, fromString(y));
    }





    public static boolean isWeekdayFormat(String... args) {
        int i = 0;
        for (String s : args) {
            for (String d : wk.keySet()) {
                if (d.equals(s.toLowerCase())) {
                    i++;
                }
            }
        }
        return i == args.length;
    }


    public static boolean dayIsBetween(String a, Calendar c, String b) {


        if (isWeekdayFormat(a, b)) {
            String x = null;
            int wd = c.get(Calendar.DAY_OF_WEEK);
            for (Map.Entry<String, Integer> e : wk.entrySet()) {
                if (e.getValue().equals(wd)) {
                    x = e.getKey();
                    break;
                }
            }
            if (!StringUtil.hasText(x)) {
                return false;
            }
            return dayIsBetween(a, x, b);
        }

        MomentInYear ma = dayFromString(a);
        MomentInYear mx = dayFromCalendar(c, ma);
        MomentInYear mb = dayFromString(b);

        return dayMonthIsBetween(ma, mx, mb);

    }

    private static MomentInYear dayFromCalendar(Calendar c, MomentInYear m) {
        MomentInYear r = new MomentInYear();
        if (m.hasOnlyDay()) {
            r._d = c.get(Calendar.DAY_OF_MONTH);
            return r;
        }
        if (m.hasNoYear()) {
            r._d = c.get(Calendar.DAY_OF_MONTH);
            r._m = c.get(Calendar.MONTH) + 1;
            return r;
        }
        r._d = c.get(Calendar.DAY_OF_MONTH);
        r._m = c.get(Calendar.MONTH) + 1;
        r._y = c.get(Calendar.YEAR);
        return r;
    }


    public static boolean dayIsBetween(String a, String x, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();
        x = x.toLowerCase();
        if (wk.containsKey(a) && wk.containsKey(x) && wk.containsKey(b)) {
            return rollIsBetween(
                    wk.get(a),
                    wk.get(x),
                    wk.get(b)
            );
        }

        MomentInYear ma = dayFromString(a);
        MomentInYear mx = dayFromString(x);
        MomentInYear mb = dayFromString(b);
        return dayMonthIsBetween(ma, mx, mb);
    }

    public static boolean dayMonthIsBetween(MomentInYear i, MomentInYear m, MomentInYear d) {

        if (i.hasOnlyDay() &&
                m.hasOnlyDay() &&
                d.hasOnlyDay()) {
            return rollIsBetween(i.day(), m.day(), d.day());
        }

        Calendar di = calendarFromString(i.toString(), i.format());
        Calendar dm = calendarFromString(m.toString(), m.format());
        Calendar dd = calendarFromString(d.toString(), d.format());

        if (d.month() < i.month() &&
                m.hasNoYear() &&
                i.hasNoYear() &&
                d.hasNoYear()) {
            dd.add(Calendar.YEAR, 1);
        }
        return dm.after(di) && dm.before(dd);
    }


    public static Calendar calendarFromString(String i, String f) {
        SimpleDateFormat s = new SimpleDateFormat(f);
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(s.parse(i));
            return c;
        } catch (ParseException e) {
            throw new SyntaxException("invalid date " + i + " format " + f);
        }
    }

    static boolean rollIsBetween(int da, int xx, int db) {
        if (da <= db) {
            return xx > da && xx < db;
        }
        return xx + da >= da && xx + da < db + da;
    }

    public static boolean isBefore(String a, String m) {
        return isBefore(fromString(a), fromString(m));
    }

    public static boolean isBetween(MomentInDay i, MomentInDay m, MomentInDay d) {


//        if (isAfter(i, m)){
//            if (isBefore(d, i)){
//                d._h = 24 + d._h;
//                return isBefore(m, d);
//            }
//        }


        return compareIsBetween(i, m, d);
    }


    private static boolean compareIsBetween(Comparable i, Comparable m, Comparable d) {
        return i.compareTo(m) == -1 && m.compareTo(d) == -1;
    }


    public static int compareHourRefs(String a, String b) {
        return fromString(a).compareTo(fromString(b));
    }

    public static boolean isBefore(MomentInDay a, MomentInDay m) {
        return a.compareTo(m) == -1;
    }

    public static boolean isAfter(MomentInDay a, MomentInDay m) {
        return m.compareTo(a) == 1;
    }

    public static boolean isAfter(String a, String m) {
        return isAfter(fromString(a), fromString(m));
    }


    static String lead(int x) {
        return x <= 9 ? "0" + x : "" + x;
    }

    public static class MomentInYear implements Comparable<MomentInYear> {
        int _y = -1;
        int _m = -1;
        int _d = -1;

        @Override
        public String toString() {
            if (_m < 0 && _y < 0) {
                return lead(_d);
            }
            if (_y < 0) {
                return lead(_m) + "-" + lead(_d);
            }
            return _y + "-" + lead(_m) + "-" + lead(_d);
        }

        public String format() {
            if (_m < 0 && _y < 0) {
                return "dd";
            }
            if (_y < 0) {
                return "MM-dd";
            }
            return "yyyy-MM-dd";
        }

        public int day() {
            return _d;
        }

        public int year() {
            return _y;
        }

        public int month() {
            return _m;
        }

        public int compareTo(MomentInYear o) {
            if (isMinus(year(), o.year()) ||
                    isMinus(month(), o.month()) ||
                    isMinus(day(), o.day())
            ) {
                throw new LogicalException("cannot compare incomplete moments");
            }
            if (year() == o.year()) {
                if (month() == o.month()) {
                    return day() < o.day() ? -1 : ((day() == o.day()) ? 0 : 1);
                }
                return month() < o.month() ? -1 : 1;
            }
            return year() < o.year() ? -1 : 1;
        }

        public boolean hasOnlyDay() {
            return _d > 0 && _m <= 0 && _y <= 0;
        }

        public boolean hasNoYear() {
            return _d > 0 && _m > 0 && _y <= 0;
        }
    }


    static boolean isMinus(int a, int b) {
        if ((a < 0 && b > 0) || (a > 0 && b < 0)) {
            return true;
        }
        return false;
    }

    public static MomentInYear dayFromString(String x) {
        String p[] = x.split("-");
        MomentInYear m = new MomentInYear();
        try {
            if (p.length == 1) {
                m._d = Integer.parseInt(p[0]);
                return m;
            }
            if (p.length == 2) {
                m._m = Integer.parseInt(p[0]);
                m._d = Integer.parseInt(p[1]);
                return m;
            }
            if (p.length == 3) {
                m._y = Integer.parseInt(p[0]);
                m._m = Integer.parseInt(p[1]);
                m._d = Integer.parseInt(p[2]);
                return m;
            }
            return m;
        } catch (Exception e) {
            return null;
        }
    }

    public static class MomentInDay implements Comparable<MomentInDay> {
        int _h;
        int _m;
        int _s;

        public int compareTo(MomentInDay o) {
            boolean sh = hour() == o.hour()
                    || (hour() == 24 && o.hour() == 0)
                    || (hour() == 0 && o.hour() == 24);
            if (sh) {
                if (minute() == o.minute()) {
                    return second() < o.second() ? -1 : ((second() == o.second()) ? 0 : 1);
                }
                return minute() < o.minute() ? -1 : 1;
            }
            return hour() < o.hour() ? -1 : 1;
        }

        public int hour() {
            return _h;
        }

        public int minute() {
            return _m;
        }

        public int second() {
            return _s;
        }

        public boolean isMid() {
            return _h == 0 || _h == 24;
        }
    }

}
