package com.arise.canter;

import com.arise.core.tools.Assert;

import java.util.Calendar;

import static com.arise.canter.Cronus.parseHourRef;

public class CronusTest2 {

    public static void main(String[] args) {
        Calendar calendar = CronusTest.buildMock();
        calendar.set(Calendar.DAY_OF_MONTH, 11);
        calendar.set(Calendar.MONTH, 1);

        String expect;
//        expect = Cronus.parseDayRef("DAILY between_10_and_12", calendar);
//
//        Assert.assertEquals("2003-02-11", expect);
        expect = Cronus.parseDayRef("DAILY between_10_and_12 EXCEPT:FEBRUARY", calendar);
        System.out.println(expect);

    }
}
