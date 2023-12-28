package br.inmetro.gov.beacon.frontend.interfac.domain.repository;

import br.inmetro.gov.beacon.frontend.interfac.infra.PulseEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
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
    List<PulseEntity> skiplistByMinutes(ZonedDateTime anchor, ZonedDateTime target,ZonedDateTime nextHour, Pageable pageable);

    @Query(value = "SELECT distinct * from pulse p where p.time_stamp > ?2 AND p.time_stamp< ?1 and  p.time_stamp < ?3 and minute(p.time_stamp)=0",nativeQuery = true)
    List<PulseEntity> skiplistByHours(ZonedDateTime anchor, ZonedDateTime target,ZonedDateTime nextDay);

    @Query(value = "SELECT distinct * from pulse p where p.time_stamp > ?2 AND p.time_stamp< ?1 and  p.time_stamp < ?3 and hour(p.time_stamp)=0 and minute(p.time_stamp)=0",nativeQuery = true)
    List<PulseEntity> skiplistByDays(ZonedDateTime anchor, ZonedDateTime target,ZonedDateTime nextMonth);

    @Query(value = "SELECT distinct * from pulse p where p.time_stamp > ?2 AND p.time_stamp< ?1 and  p.time_stamp < ?3 and  day(p.time_stamp)=1 and hour(p.time_stamp)=0 and minute(p.time_stamp)=0",nativeQuery = true)
    List<PulseEntity> skiplistByMonths(ZonedDateTime anchor, ZonedDateTime target,ZonedDateTime nextYear);

    @Query(value = "SELECT distinct * from pulse p where p.time_stamp > ?2 AND p.time_stamp< ?1 and month(p.time_stamp)=1 and  day(p.time_stamp)=1 and hour(p.time_stamp)=0 and minute(p.time_stamp)=0",nativeQuery = true)
    List<PulseEntity> skiplistByYears(ZonedDateTime anchor, ZonedDateTime target);


    String fullSkipList = ""+
            "select * from "+
                    "( "+
                            "SELECT * from pulse p where p.pulse_index =  ?2 "+
                                "union "+
                            "SELECT distinct * from pulse p where p.time_stamp> ?2 AND p.time_stamp < ?6 "+
                                "union "+
                            "SELECT distinct * from pulse p where p.time_stamp > ?2 AND  p.time_stamp < ?5 and minute(p.time_stamp)=0 "+
                                "union "+
                            "SELECT distinct * from pulse p where p.time_stamp > ?2 AND  p.time_stamp < ?4 and hour(p.time_stamp)=0 and minute(p.time_stamp)=0 "+
                                "union "+
                            "SELECT distinct * from pulse p where p.time_stamp > ?2 AND p.time_stamp < ?3 and  day(p.time_stamp)=1 and hour(p.time_stamp)=0 and minute(p.time_stamp)=0 "+
                                "union "+
                            "SELECT distinct * from pulse p where p.time_stamp in ?7 "+
                                "union "+
                            "SELECT * from pulse p where p.pulse_index =  ?1 "+
                    ") p order by p.time_stamp";


    @Query(value = fullSkipList, nativeQuery = true)
    List<PulseEntity> getFullSkiplist(ZonedDateTime anchor, ZonedDateTime target, ZonedDateTime nextYear, ZonedDateTime nextMonth, ZonedDateTime nextDay, ZonedDateTime nextHour, List<ZonedDateTime> firstDaysOfYears, Pageable pageable);


    String countFullSkipList = ""+
            "select count(id) from "+
            "( "+
            "SELECT id from pulse p where p.pulse_index =  ?2 "+
            "union "+
            "SELECT distinct id from pulse p where p.time_stamp> ?2 AND p.time_stamp < ?6 "+
            "union "+
            "SELECT distinct id from pulse p where p.time_stamp > ?2 AND p.time_stamp < ?5 and minute(p.time_stamp)=0 "+
            "union "+
            "SELECT distinct id from pulse p where p.time_stamp > ?2 AND p.time_stamp < ?4 and hour(p.time_stamp)=0 and minute(p.time_stamp)=0 "+
            "union "+
            "SELECT distinct id from pulse p where p.time_stamp > ?2 AND p.time_stamp < ?3 and  day(p.time_stamp)=1 and hour(p.time_stamp)=0 and minute(p.time_stamp)=0 "+
            "union "+
            "SELECT distinct id from pulse p where p.time_stamp in ?7"+
            "union "+
            "SELECT id from pulse p where p.pulse_index =  ?1 "+
            ") p";
    @Cacheable(value = "countSkipLists",key = "#anchor.toInstant().toEpochMilli().toString()+#target.toInstant().toEpochMilli().toString()")
    @Query(value = countFullSkipList, nativeQuery = true)
    Integer countFullSkiplist(ZonedDateTime anchor, ZonedDateTime target, ZonedDateTime nextYear, ZonedDateTime nextMonth, ZonedDateTime nextDay, ZonedDateTime nextHour,List<ZonedDateTime> firstDaysOfYears);


    String countskipListByChain = ""+
            "select count(id) from "+
            "( "+
            "SELECT id from pulse p where p.pulse_index =  ?2 AND p.chain_index=?7 "+
            "union "+
            "SELECT distinct id from pulse p where p.pulse_index> ?2 AND  p.time_stamp < ?6  AND p.chain_index=?7 "+
            "union "+
            "SELECT distinct id from pulse p where p.pulse_index > ?2 AND p.time_stamp < ?5 and minute(p.time_stamp)=0 AND p.chain_index=?7 "+
            "union "+
            "SELECT distinct id from pulse p where p.pulse_index > ?2 AND p.time_stamp < ?4 and hour(p.time_stamp)=0 and minute(p.time_stamp)=0 AND p.chain_index=?7 "+
            "union "+
            "SELECT distinct id from pulse p where p.pulse_index > ?2 AND p.time_stamp < ?3 and  day(p.time_stamp)=1 and hour(p.time_stamp)=0 and minute(p.time_stamp)=0 AND p.chain_index=?7 "+
            "union "+
            "SELECT distinct id from pulse p where p.pulse_index in ?7 "+
            "union "+
            "SELECT id from pulse p where p.pulse_index =  ?1 AND p.chain_index=?8 "+
            ") p";
    @Cacheable(value = "countSkipListsByChainAndIndexes",key = "#anchorIdx.toString()+\":\"+#targetIdx.toString()+\":\"+#chainIdx.toString()")
    @Query(value = countskipListByChain, nativeQuery = true)
    Integer countSkiplistByChain(Long anchorIdx, Long targetIdx, ZonedDateTime nextYear, ZonedDateTime nextMonth, ZonedDateTime nextDay, ZonedDateTime nextHour, List<ZonedDateTime>firstDaysOfYears,Long chainIdx);


    String skipListByChainAndIndex = ""+
            "select * from "+
            "( "+
            "SELECT * from pulse p where p.pulse_index =  ?2 AND p.chain_index=?7 "+
            "union "+
            "SELECT distinct * from pulse p where p.pulse_index> ?2 AND p.time_stamp < ?6 AND p.chain_index=?8 "+
            "union "+
            "SELECT distinct * from pulse p where p.pulse_index > ?2 AND p.time_stamp < ?5 and minute(p.time_stamp)=0 AND p.chain_index=?8 "+
            "union "+
            "SELECT distinct * from pulse p where p.pulse_index > ?2 AND p.time_stamp < ?4 and hour(p.time_stamp)=0 and minute(p.time_stamp)=0 AND p.chain_index=?8 "+
            "union "+
            "SELECT distinct * from pulse p where p.pulse_index > ?2 AND p.time_stamp < ?3 and  day(p.time_stamp)=1 and hour(p.time_stamp)=0 and minute(p.time_stamp)=0 AND p.chain_index=?8 "+
            "union "+
            "SELECT distinct * from pulse p where p.pulse_index in ?7 AND p.chain_index=?8"+
            "union "+
            "SELECT * from pulse p where p.pulse_index =  ?1 AND p.chain_index=?8 "+
            ") p order by p.time_stamp";

    @Query(value = skipListByChainAndIndex, nativeQuery = true)
    List<PulseEntity> getSkiplistByChainAndIndex(Long anchorIdx, Long targetIdx, ZonedDateTime nextYear, ZonedDateTime nextMonth, ZonedDateTime nextDay, ZonedDateTime nextHour, List<ZonedDateTime> firstDaysOfYears,Long chainIdx, Pageable pageable);


}
