package com.arise.canter;


import java.util.Calendar;

import static com.arise.canter.Cronus.isAfter;
import static com.arise.canter.Cronus.isBefore;
import static com.arise.canter.Cronus.isBetween;
import static com.arise.canter.Cronus.parseHourRef;
import static com.arise.core.tools.Assert.assertEquals;
import static com.arise.core.tools.Assert.assertFalse;
import static com.arise.core.tools.Assert.assertTrue;

public class CronusTest {
    public static void test(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2003);
        calendar.set(Calendar.MONTH, 5);
        calendar.set(Calendar.DAY_OF_MONTH, 18);
        calendar.set(Calendar.HOUR_OF_DAY, 17);
        calendar.set(Calendar.MINUTE, 15);
        calendar.set(Calendar.SECOND, 00);
        String expect = parseHourRef("EACH_SECOND", calendar);
        assertEquals("17:15:00", expect);

        calendar.set(Calendar.SECOND, 25);
        expect = parseHourRef("EACH_SECOND", calendar);
        assertEquals("17:15:25", expect);

        calendar.set(Calendar.SECOND, 7);
        expect = parseHourRef("EACH_7_SECONDS", calendar);
        assertEquals("17:15:07", expect);

        calendar.set(Calendar.SECOND, 21);
        expect = parseHourRef("EACH_7_SECONDS", calendar);
        assertEquals("17:15:21", expect);

        calendar.set(Calendar.SECOND, 15);
        expect = parseHourRef("EACH_7_SECONDS", calendar);
        assertEquals("xx:xx:xx", expect);

        assertTrue(isBetween("17:00:00", calendar, "17:15:16"));

        calendar.set(Calendar.SECOND, 15);
        expect = parseHourRef("EACH_7_SECONDS BETWEEN_17:00:00_AND_17:16:20", calendar);
        assertEquals("xx:xx:xx", expect);

        calendar.set(Calendar.SECOND, 10);
        expect = parseHourRef("EACH_10_SECONDS BETWEEN_17:00:00_AND_18:16:20", calendar);
        assertEquals("17:15:10", expect);

    }

    public static void main(String[] args) {
        CronusTest.testStrings();
        CronusTest.test();
    }

    private static void testStrings() {
        boolean isBefore = isBefore("06:00:00", "07:00:00");
        assertTrue(isBefore);

        isBefore = isBefore("07:00:00", "06:01:00");
        assertFalse(isBefore);

        isBefore = isBefore("06:00:00", "06:01:00");
        assertTrue(isBefore);

        isBefore = isBefore("06:01:00", "06:00:00");
        assertFalse(isBefore);
        isBefore = isBefore("06:00:00", "06:00:01");
        assertTrue(isBefore);


        isBefore = isBefore("06:00:01", "06:00:00");
        assertFalse(isBefore);
        assertTrue(isAfter( "06:00:00", "06:00:01"));

        assertTrue(isAfter("05:99:99", "06:00:00"));
        assertTrue(isAfter("06:00:59", "06:01:00"));
        assertTrue(isAfter("06:00:02", "06:01:01"));
        assertFalse(isBefore("06:00:00", "06:00:00"));
        assertFalse(isAfter("06:00:00", "06:00:00"));

        assertTrue(isAfter("07:22:20", "07:24:00"));
        assertTrue(isBefore("07:20:20", "07:22:20"));

        assertTrue(isBetween("07:20:20", "07:22:20", "07:24:00"));
        assertTrue(isBetween("07:22:21", "07:22:22", "07:22:23"));
        assertTrue(isBetween("07:20:20", "08:22:20", "09:24:00"));
    }
}