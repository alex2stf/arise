package com.arise.core.tools;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by alex2 on 23/04/2024.
 */
public class DateUtil {
    public static String nowHour(){
        return new SimpleDateFormat("HH:ss").format(new Date());
    }
}
