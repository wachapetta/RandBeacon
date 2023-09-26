package com.example.beacon.interfac.domain.service;

import com.example.beacon.interfac.api.dto.PagedResponseDto;
import com.example.beacon.interfac.domain.repository.PulsesRepository;
import com.example.beacon.interfac.api.dto.PulseDto;
import com.example.beacon.interfac.infra.PulseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class QuerySequencePulsesService {

    private final PulsesRepository pulsesRepository;
    private final Environment env;

    @Autowired
    public QuerySequencePulsesService(PulsesRepository pulsesRepository, Environment env) {
        this.pulsesRepository = pulsesRepository;
        this.env = env;

    }

    public List<PulseDto> sequence(ZonedDateTime timeStamp1, ZonedDateTime timeStamp2){
        long fiveDays = 7200;
        long between = ChronoUnit.MINUTES.between(timeStamp1, timeStamp2);

        if (between > fiveDays){
            throw new BadRequestException("Maximum pulses per request: 7200");
        }

        List<PulseDto> dtos = new ArrayList<>();
        List<PulseEntity> sequence = pulsesRepository.findSequence(timeStamp1, timeStamp2);
        sequence.forEach(entity -> dtos.add(new PulseDto(entity)));
        return dtos;
    }


    @Transactional(readOnly = true)
    public PagedResponseDto skiplist(ZonedDateTime anchor, ZonedDateTime target, int offset, int limit){

        ZonedDateTime nextYear =  target.with(TemporalAdjusters.firstDayOfNextYear()).withHour(0).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime nextMonth = target.with(TemporalAdjusters.firstDayOfNextMonth()).withHour(0).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime nextDay = target.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime nextHour = target.plusHours(1).withMinute(0).withSecond(0).withNano(0);

        List<PulseDto> dtos = new ArrayList<>();
        List<PulseEntity> sequence = new ArrayList<PulseEntity>();

        int pageIndex = offset / limit;
        Pageable page = PageRequest.of(pageIndex,limit);

        int countSkipList = pulsesRepository.countSkiplist(anchor,target,nextYear,nextMonth,nextDay,nextHour);

        sequence.addAll(pulsesRepository.getSkiplist(anchor,target,nextYear,nextMonth,nextDay,nextHour,page));

        sequence.forEach(entity -> dtos.add(new PulseDto(entity)));

        PagedResponseDto pagedResponseDto = new PagedResponseDto(dtos);

        String version = env.getProperty("beacon.url.version");
        String first = env.getProperty("beacon.url")+"/beacon/"+ version +"/skiplist?anchorTime="+ anchor.toInstant().toEpochMilli()+"&targetTime="+target.toInstant().toEpochMilli();
        String previous=countSkipList<limit || pageIndex==0 ?"null":env.getProperty("beacon.url")+"/beacon/"+ version +"/skiplist?anchorTime="+ anchor.toInstant().toEpochMilli()+"&targetTime="+target.toInstant().toEpochMilli()+"&offset="+ (pageIndex-1)*limit +"&limit="+limit;
        String next= pageIndex*limit > countSkipList ?"null":env.getProperty("beacon.url")+"/beacon/"+ version +"/skiplist?anchorTime="+ anchor.toInstant().toEpochMilli()+"&targetTime="+target.toInstant().toEpochMilli()+"&offset="+ (pageIndex+1)*limit +"&limit="+limit;

        int lastPage = (int) Math.ceil(countSkipList / limit);
        String last= env.getProperty("beacon.url")+"/beacon/"+ version +"/skiplist?anchorTime="+ anchor.toInstant().toEpochMilli()+"&targetTime="+target.toInstant().toEpochMilli()+"&offset="+ lastPage +"&limit="+limit;

        Map<String,String> links = new LinkedHashMap<String,String>();
        links.put("first", first);
        links.put("previous",previous);
        links.put("next",next);
        links.put("last",last);

        pagedResponseDto.setLinks(links);
        pagedResponseDto.setIncomplete(countSkipList!=sequence.size());
        pagedResponseDto.setTotalCount(countSkipList);

        return pagedResponseDto;
    }

}
