package com.arise.core.tools;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by alex2 on 23/04/2024.
 */
public class DateUtil {
    public static String nowHour(){
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }


    public static String nowAddMillis(int milis){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MILLISECOND, milis);
        return new SimpleDateFormat("HH:mm:ss").format(c.getTime());
    }
}
