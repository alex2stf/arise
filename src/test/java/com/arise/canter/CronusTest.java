package com.arise.canter;


import com.arise.core.exceptions.LogicalException;
import com.arise.core.exceptions.SyntaxException;
import com.arise.core.models.Tuple2;
import com.arise.core.tools.Assert;

import javax.security.auth.login.CredentialNotFoundException;
import javax.security.auth.login.LoginException;
import java.util.Calendar;

import static com.arise.canter.Cronus.dayFromString;
import static com.arise.canter.Cronus.dayIsBetween;
import static com.arise.canter.Cronus.dayMonthIsBetween;
import static com.arise.canter.Cronus.isAfter;
import static com.arise.canter.Cronus.isBefore;
import static com.arise.canter.Cronus.isBetween;
import static com.arise.canter.Cronus.isWeekdayFormat;
import static com.arise.canter.Cronus.parseDayRef;
import static com.arise.canter.Cronus.parseHourRef;
import static com.arise.canter.Cronus.rollIsBetween;
import static com.arise.core.models.Tuple2.numInt;
import static com.arise.core.tools.Assert.assertEquals;
import static com.arise.core.tools.Assert.assertFalse;
import static com.arise.core.tools.Assert.assertTrue;

public class CronusTest {
    public static void test(){
        Calendar calendar = buildMock();

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

    public static void testCompares(){
        assertEquals(-1, Integer.valueOf(1).compareTo(2));
        assertEquals(0, Integer.valueOf(2).compareTo(2));
        assertEquals(1, Integer.valueOf(2).compareTo(1));

        assertEquals(-1, Cronus.compareHourRefs("23:00:00", "24:00:00"));
        assertEquals(-1, Cronus.compareHourRefs("23:12:00", "23:13:00"));
        assertEquals(-1, Cronus.compareHourRefs("23:12:00", "23:12:01"));
        assertEquals(0, Cronus.compareHourRefs("23:12:00", "23:12:00"));
        assertEquals(0, Cronus.compareHourRefs("23:00:00", "23:00:00"));
        assertEquals(1, Cronus.compareHourRefs("24:00:00", "23:00:00"));
        assertEquals(1, Cronus.compareHourRefs("24:01:00", "24:00:00"));
        assertEquals(1, Cronus.compareHourRefs("24:01:01", "24:01:00"));


        assertTrue(isBetween("07:20:20", "07:22:20", "07:24:00"));
        assertTrue(isBetween("19:20:20", "07:22:20", "07:24:00"));
        assertFalse(isBetween("19:20:20", "07:22:20", "06:24:00"));

        assertTrue(isBetween("23:00:00", "00:00:00", "01:00:00"));

    }

    public static void main(String[] args) {
        CronusTest.testStrings();
        CronusTest.test();
        CronusTest.testCompares();
        CronusTest.testDays();
        CronusTest.testDays2();
        CronusTest.testDays3();
    }

    private static void testDays3() {
        assertTrue(
                dayIsBetween("01", "02", "03")
        );

        Cronus.MomentInYear m = Cronus.dayFromString("23");
        assertEquals("23", m.toString());

        m = Cronus.dayFromString("9");
        assertEquals("09", m.toString());
        assertEquals("dd", m.format());

        m = Cronus.dayFromString("12-23");
        assertEquals("12-23", m.toString());
        assertEquals("MM-dd", m.format());

        m = Cronus.dayFromString("1-2");
        assertEquals("01-02", m.toString());
        assertEquals("MM-dd", m.format());


        m = Cronus.dayFromString("1998-8-21");
        assertEquals("1998-08-21", m.toString());
        assertEquals("yyyy-MM-dd", m.format());


        assertTrue(
                dayIsBetween("12-12", "12-14", "01-05")
        );

        assertTrue(
                dayIsBetween("11-12", "12-14", "10-05")
        );

        assertFalse(
                dayIsBetween("01-12", "03-14", "02-05")
        );


        assertFalse(
                dayIsBetween("01-12", "01-16", "01-13")
        );


        assertFalse(
                dayIsBetween("02-12", "02-16", "02-13")
        );


        assertTrue(
                dayIsBetween("02-12", "02-16", "01-13")
        );
        Calendar c = buildMock();
        c.set(Calendar.DAY_OF_MONTH, 18);
        String ref = Cronus.parseDayRef("DAILY between_10_and_11", c);
        assertEquals("xx:xx:xx", ref);


        ref = Cronus.parseDayRef("DAILY between_17_and_19", c);
        assertEquals("2003-06-18", ref);

        ref = Cronus.parseDayRef("DAILY between_06-17_and_08-19", c);
        assertEquals("2003-06-18", ref);

        ref = Cronus.parseDayRef("DAILY between_07-07_and_08-09", c);
        assertEquals("xx:xx:xx", ref);

        c.set(Calendar.MONTH, 1); //february
        ref = Cronus.parseDayRef("DAILY between_2003-01-07_and_2003-03-09", c);
        assertEquals("2003-02-18", ref);
    }

    private static  Calendar buildMock(){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, 2003);
        c.set(Calendar.MONTH, 5);
        c.set(Calendar.DAY_OF_MONTH, 18);
        c.set(Calendar.HOUR_OF_DAY, 17);
        c.set(Calendar.MINUTE, 15);
        c.set(Calendar.SECOND, 00);
        return c;
    }

    private static void testDays2() {
        Calendar c = buildMock();

        c.set(Calendar.DAY_OF_MONTH, 11);
        assertTrue(
                dayIsBetween("10", "11", "12")
        );

        assertTrue(
                dayIsBetween("31", "11", "12")
        );

        assertFalse(
                dayIsBetween("12", "16", "15")
        );

        assertFalse(
                dayIsBetween("03", "02", "04")
        );

        assertFalse(
                rollIsBetween(1, 1, 3)
        );

       Cronus.MomentInYear m = dayFromString("1999-04-21");
        assertEquals(m.year(), 1999);
        assertEquals(m.month(), 4);
        assertEquals(m.day(), 21);


        m = dayFromString("03-22");
        assertEquals(m.year(), -1);
        assertEquals(m.month(), 3);
        assertEquals(m.day(), 22);

        m = dayFromString("22");
        assertEquals(m.year(), -1);
        assertEquals(m.month(), -1);
        assertEquals(m.day(), 22);

        assertEquals(
                0,
                dayFromString("1999-04-21").compareTo(dayFromString("1999-04-21"))
        );

        assertEquals(
                -1,
                dayFromString("1999-04-20").compareTo(dayFromString("1999-04-21"))
        );

        assertEquals(
                1,
                dayFromString("1999-04-22").compareTo(dayFromString("1999-04-21"))
        );

        assertEquals(
                -1,
                dayFromString("1999-04-22").compareTo(dayFromString("1999-05-22"))
        );

        assertEquals(
                1,
                dayFromString("1999-06-22").compareTo(dayFromString("1999-05-22"))
        );

        assertEquals(
                -1,
                dayFromString("1999-06-22").compareTo(dayFromString("2000-06-22"))
        );

        assertEquals(
                1,
                dayFromString("2001-06-22").compareTo(dayFromString("2000-06-22"))
        );

        Assert.assertThrows(new Runnable() {
            @Override
            public void run() {
                dayFromString("2001-06-22").compareTo(dayFromString("06-22"));
            }
        }, LogicalException.class);


        Assert.assertThrows(new Runnable() {
            @Override
            public void run() {
                dayFromString("06-22").compareTo(dayFromString("06"));
            }
        }, LogicalException.class);


        assertEquals(
                -1,
                dayFromString("06-22").compareTo(dayFromString("07-22"))
        );

        assertEquals(
                0,
                dayFromString("06-22").compareTo(dayFromString("06-22"))
        );

        assertEquals(
                1,
                dayFromString("07-22").compareTo(dayFromString("06-22"))
        );

        assertEquals(
                -1,
                dayFromString("1").compareTo(dayFromString("2"))
        );

        assertEquals(
                0,
                dayFromString("1").compareTo(dayFromString("1"))
        );

        assertEquals(
                1,
                dayFromString("2").compareTo(dayFromString("1"))
        );

    }

    private static void testDays() {
        Calendar c = buildMock();

        String ref = parseDayRef("DAILY", c);
        assertEquals("2003-06-18", ref);

        assertTrue(
                dayIsBetween("monday", "tuesday", "wednesday")
        );

        assertTrue(
                dayIsBetween("monday", "tuesday", "thursday")
        );
        assertTrue(
                dayIsBetween("tuesday", "thursday", "sunday")
        );

        assertTrue(
                dayIsBetween("sunday", "monday", "tuesday")
        );

        assertFalse(
                dayIsBetween("sunday", "monday", "sunday")
        );

        assertFalse(
                dayIsBetween("sunday", "thursday", "monday")
        );

        assertFalse(
                dayIsBetween("friday", "thursday", "thursday")
        );


        assertFalse(
                dayIsBetween("saturday", "friday", "thursday")
        );


        c.set(Calendar.DAY_OF_MONTH, 18);
        assertTrue(
                dayIsBetween("wednesday", c, "saturday")
        );

        assertTrue(
                dayIsBetween("saturday", c, "friday")
        );

        assertTrue(
                isWeekdayFormat("saturday", "friday")
        );

        assertTrue(
                isWeekdayFormat("saturday", "friday", "MoNdAy", "sunday", "sunday")
        );

        assertFalse(
                isWeekdayFormat("satURday", "moNday", "xxx")
        );

        ref = parseDayRef("DAILY between_monday_and_friday", c);
        assertEquals("2003-06-18", ref);

        ref = parseDayRef("DAILY between_monday_and_SUNDAY", c);
        assertEquals("2003-06-18", ref);
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