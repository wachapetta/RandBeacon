package br.inmetro.gov.beacon.frontend.vdf.infra.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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

    public static ZonedDateTime longToExactDateTime(String data){
        Long millis = Long.parseLong(data);
        if (data.length() == 10){
            millis = millis*1000;
        }
        ZonedDateTime localDateTime = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).truncatedTo(ChronoUnit.SECONDS);

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

        List<Integer> tmp = new ArrayList<Integer>();
        tmp.addAll(startsAtMinutes);
        tmp.replaceAll(integer -> integer ==0 ? integer=60:integer);

        int minute = dateTime.getMinute();

        int next =0;

        tmp.removeIf(integer -> integer <= minute);
        tmp.sort(Integer::compareTo);
        next = tmp.get(0);


        return dateTime.plus(Duration.ofMinutes(next-minute)).withSecond(0).withNano(0);
    }

}
