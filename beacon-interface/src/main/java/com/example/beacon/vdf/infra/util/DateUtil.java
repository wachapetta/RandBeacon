package com.example.beacon.vdf.infra.util;

import groovyjarjarantlr4.v4.parse.ATNBuilder;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class DateUtil {

    public static ZonedDateTime getCurrentTrucatedZonedDateTime(){
        ZonedDateTime now = ZonedDateTime.now()
                .truncatedTo(ChronoUnit.MINUTES)
                .withZoneSameInstant((ZoneOffset.UTC).normalized());
        return now;
    }

    public static long datetimeToMilli(ZonedDateTime time){
        return time.toInstant().toEpochMilli();
    }

    public static ZonedDateTime longToLocalDateTime(String data){
        Long millis = Long.parseLong(data);
        if (data.length() == 10){
            millis = millis*1000;
        }

        // atual
        ZonedDateTime localDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis),
                ZoneId.of("UTC")).truncatedTo(ChronoUnit.MINUTES);

        return localDateTime;
    }


    public static String getTimeStampFormated(ZonedDateTime timestamp){
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSz");
        String format = timestamp.withZoneSameInstant((ZoneOffset.UTC).normalized()).format(dateTimeFormatter);
        return format;
    }

    public static long getMinutesForNextRun(ZonedDateTime dateTimeActual, ZonedDateTime nextRun){
        return ChronoUnit.MINUTES.between(dateTimeActual, nextRun);
    }

    public static ZonedDateTime getTimestampOfNextRun(ZonedDateTime dateTime, List<Integer> startsAtMinutes){

        startsAtMinutes.replaceAll(integer -> integer ==0 ? integer=60:integer);

        int minute = dateTime.getMinute();

        int next =0;

        startsAtMinutes.removeIf(integer -> integer <= minute);
        startsAtMinutes.sort(Integer::compareTo);
        next = startsAtMinutes.get(0);


        return dateTime.plus(Duration.ofMinutes(next-minute)).withSecond(0).withNano(0);
    }

}
