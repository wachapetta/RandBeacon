package com.example.beacon.interfac.domain.repository;

import com.example.beacon.interfac.infra.PulseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface PulsesRepository extends JpaRepository<PulseEntity, Long>, PulsesQueries {

    @Query(value = "SELECT * FROM pulse where pulse.time_stamp > ?1 order by pulse.time_stamp limit 1;", nativeQuery = true)
    PulseEntity findNext(ZonedDateTime timeStamp);

    @Query(value = "SELECT * FROM pulse where pulse.time_stamp < ?1 order by pulse.time_stamp desc limit 1;", nativeQuery = true)
    PulseEntity findPrevious(ZonedDateTime timeStamp);

    @Query(value = "SELECT distinct p from PulseEntity p left join fetch p.listValueEntities l where p.timeStamp >= ?1 and p.timeStamp <=?2 order by p.timeStamp")
    List<PulseEntity> findSequence(ZonedDateTime timeStamp1, ZonedDateTime timeStamp2);


    @Query(value = "SELECT distinct * from pulse p where p.time_stamp> ?2 AND p.time_stamp< ?1 AND  p.time_stamp < ?3", nativeQuery = true)
    List<PulseEntity> skiplistByMinutes(ZonedDateTime anchor, ZonedDateTime target,ZonedDateTime nextHour);

    @Query(value = "SELECT distinct * from pulse p where p.time_stamp > ?2 AND p.time_stamp< ?1 and  p.time_stamp < ?3 and minute(p.time_stamp)=0",nativeQuery = true)
    List<PulseEntity> skiplistByHours(ZonedDateTime anchor, ZonedDateTime target,ZonedDateTime nextDay);

    @Query(value = "SELECT distinct * from pulse p where p.time_stamp > ?2 AND p.time_stamp< ?1 and  p.time_stamp < ?3 and hour(p.time_stamp)=0 and minute(p.time_stamp)=0",nativeQuery = true)
    List<PulseEntity> skiplistByDays(ZonedDateTime anchor, ZonedDateTime target,ZonedDateTime nextMonth);

    @Query(value = "SELECT distinct * from pulse p where p.time_stamp > ?2 AND p.time_stamp< ?1 and  p.time_stamp < ?3 and  day(p.time_stamp)=1 and hour(p.time_stamp)=0 and minute(p.time_stamp)=0",nativeQuery = true)
    List<PulseEntity> skiplistByMonths(ZonedDateTime anchor, ZonedDateTime target,ZonedDateTime nextYear);

    @Query(value = "SELECT distinct * from pulse p where p.time_stamp > ?2 AND p.time_stamp< ?1 and month(p.time_stamp)=1 and  day(p.time_stamp)=1 and hour(p.time_stamp)=0 and minute(p.time_stamp)=0",nativeQuery = true)
    List<PulseEntity> skiplistByYears(ZonedDateTime anchor, ZonedDateTime target);
}
