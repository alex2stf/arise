package com.arise.canter;

import com.arise.core.tools.Util;

import java.util.Calendar;

/**
 * Created by alex2 on 17/06/2024.
 */
public class CronusTest3 {

    public static void main(String[] args) {
        System.out.println(
                Cronus.parseDayRef("2024-01-01", Util.nowCalendar())
        );

        System.out.println(
                Cronus.parseDayRef("monday_tuesday", Util.nowCalendar())
        );

        System.out.println(
                Cronus.parseDayRef("annually:08-01", Util.nowCalendar())
        );

//        Cronus.matchMoment(Util.nowCalendar(), "annually:01-08", "12:00:00");
//        for(int i = 1; i < 30; i++) {
//            Cronus.matchMoment(Util.nowCalendar(), "random_day:30:id" + i + " EXCEPT:june", "10:20:00");
//        }
    }
}
