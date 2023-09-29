package com.example.beacon.vdf.infra.util;

import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DateUtilTest {


    List<Integer> each30min = new ArrayList();
    List<Integer> each2min = new ArrayList();

    @Before
    public void before(){
        each30min.add(0);
        each30min.add(30);

        each2min.add(0);
        each2min.add(2);
        each2min.add(4);
    }

    @Test
    public void datetimeToMilli() {
        ZonedDateTime parse = ZonedDateTime.parse("2019-09-07T20:11:00.000Z");
        long l = parse.toInstant().toEpochMilli();
        assertEquals(1567887060000L, l);
    }

    @Test
    public void longToLocalDateTimeTest(){
        ZonedDateTime zonedDateTime = DateUtil.longToLocalDateTime("1567887077666");
        assertEquals("2019-09-07T20:11:00.000Z", DateUtil.getTimeStampFormated(zonedDateTime));
    }

    @Test
    public void nextExecutionInMinutes() {
//        ZonedDateTime actual = ZonedDateTime.parse("2019-08-23T13:04:00.000Z");
        ZonedDateTime nextRun = DateUtil.getTimestampOfNextRun(ZonedDateTime.parse("2019-08-23T13:04:00.000Z"),each30min);
        long i = DateUtil.getMinutesForNextRun(ZonedDateTime.parse("2019-08-23T13:04:00.000Z"), nextRun);
        assertEquals(i, 26 );
    }

    @Test
    public void nextTimestampExecutionBefore30() {
        ZonedDateTime start = ZonedDateTime.parse("2019-08-23T13:04:00.000Z");
        ZonedDateTime actual = DateUtil.getTimestampOfNextRun(start,each30min);
        assertEquals(ZonedDateTime.parse("2019-08-23T13:30:00.000Z"), actual);
    }

    @Test
    public void nextTimestampExecutionAfter30() {
        ZonedDateTime start = ZonedDateTime.parse("2019-08-23T13:31:00.000Z");
        ZonedDateTime actual = DateUtil.getTimestampOfNextRun(start,each30min);
        assertEquals(ZonedDateTime.parse("2019-08-23T14:00:00.000Z"), actual);
    }

    @Test
    public void nextTimestampExecution30() {
        ZonedDateTime start = ZonedDateTime.parse("2019-08-23T13:30:00.000Z");
        ZonedDateTime actual = DateUtil.getTimestampOfNextRun(start,each30min);
        assertEquals(ZonedDateTime.parse("2019-08-23T14:00:00.000Z"), actual);
    }


    @Test
    public void nextTimestamp1() {
        ZonedDateTime start = ZonedDateTime.parse("2019-08-23T13:58:00.000Z");
        ZonedDateTime actual = DateUtil.getTimestampOfNextRun(start,each2min);
        assertEquals(ZonedDateTime.parse("2019-08-23T14:00:00.000Z"), actual);
    }

    @Test
    public void nextTimestamp2() {
        ZonedDateTime start = ZonedDateTime.parse("2019-08-23T13:00:00.000Z");
        ZonedDateTime actual = DateUtil.getTimestampOfNextRun(start,each2min);
        assertEquals(ZonedDateTime.parse("2019-08-23T13:02:00.000Z"), actual);
    }

    @Test
    public void nextTimestamp3() {
        ZonedDateTime start = ZonedDateTime.parse("2019-08-23T13:02:00.000Z");
        ZonedDateTime actual = DateUtil.getTimestampOfNextRun(start,each2min);
        assertEquals(ZonedDateTime.parse("2019-08-23T13:04:00.000Z"), actual);
    }


}