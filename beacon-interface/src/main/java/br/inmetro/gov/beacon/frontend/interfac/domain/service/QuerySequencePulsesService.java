package br.inmetro.gov.beacon.frontend.interfac.domain.service;

import br.inmetro.gov.beacon.frontend.interfac.domain.repository.PulsesRepository;
import br.inmetro.gov.beacon.frontend.interfac.api.dto.PagedResponseDto;
import br.inmetro.gov.beacon.frontend.interfac.api.dto.PulseDto;
import br.inmetro.gov.beacon.frontend.interfac.infra.PulseEntity;
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
            throw new BadRequestException("Maximum pulses per request: 7200. Try the API version 2.1!");
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

        List<ZonedDateTime> firstDaysOfYears = new ArrayList();
        firstDaysOfYears.add(null);
        ZonedDateTime link = target.with(TemporalAdjusters.firstDayOfNextYear()).withHour(0).withMinute(0).withSecond(0).withNano(0);

        while(link.isBefore( anchor)){
            firstDaysOfYears.add(link);
            link = link.plusYears(1);
        }

        List<PulseDto> dtos = new ArrayList<>();
        List<PulseEntity> sequence = new ArrayList<>();

        int pageIndex = offset / limit;
        Pageable page = PageRequest.of(pageIndex,limit);

        long countSkipList = pulsesRepository.countFullSkiplist(anchor,target,nextYear,nextMonth,nextDay,nextHour,firstDaysOfYears);

        sequence.addAll(pulsesRepository.getFullSkiplist(anchor,target,nextYear,nextMonth,nextDay,nextHour,firstDaysOfYears,page));

        sequence.forEach(entity -> dtos.add(new PulseDto(entity)));

        PagedResponseDto pagedResponseDto = new PagedResponseDto(dtos);

        String version = env.getProperty("beacon.url.version");
        String first = env.getProperty("beacon.url")+"/beacon/"+ version +"/skiplist/time/"+ target.toInstant().toEpochMilli()+"/"+anchor.toInstant().toEpochMilli();


        int previousIndex = pageIndex - 1;
        int nextIndex = pageIndex + 1;

        int lastPage = (int) Math.ceil(countSkipList / limit);

        String previous=countSkipList<limit || previousIndex<0 ?"null":env.getProperty("beacon.url")+"/beacon/"+ version +"/skiplist/time/"+ target.toInstant().toEpochMilli()+"/"+anchor.toInstant().toEpochMilli()+"?offset="+ previousIndex * limit +"&limit="+limit;
        String next= nextIndex*limit > countSkipList ?"null":env.getProperty("beacon.url")+"/beacon/"+ version +"/skiplist/time/"+ target.toInstant().toEpochMilli()+"/"+anchor.toInstant().toEpochMilli()+"?offset="+ nextIndex * limit +"&limit="+limit;

        String last= countSkipList<limit? first: env.getProperty("beacon.url")+"/beacon/"+ version +"/skiplist/time/"+ target.toInstant().toEpochMilli()+"/"+anchor.toInstant().toEpochMilli()+"?offset="+ lastPage*limit +"&limit="+limit;

        Map<String,String> links = new LinkedHashMap<>();
        links.put("first", first);
        links.put("previous",previous);
        links.put("next",next);
        links.put("last",last);

        pagedResponseDto.setLinks(links);
        pagedResponseDto.setIncomplete(countSkipList!=sequence.size());
        pagedResponseDto.setTotalCount(countSkipList);

        return pagedResponseDto;
    }


    @Transactional(readOnly = true)
    public PagedResponseDto skiplist(ZonedDateTime anchorTime, ZonedDateTime targetTime, int offset, int limit,long chain){


            PulseEntity anchor = pulsesRepository.findByTimestampAndChain(anchorTime.truncatedTo(ChronoUnit.MINUTES).truncatedTo(ChronoUnit.SECONDS).truncatedTo(ChronoUnit.MILLIS),chain);
            PulseEntity target = pulsesRepository.findByTimestampAndChain(targetTime.truncatedTo(ChronoUnit.MINUTES).truncatedTo(ChronoUnit.SECONDS).truncatedTo(ChronoUnit.MILLIS),chain);

            if (anchor == null && target == null)
                throw new BadRequestException("There is no pulse at searched timestamps in this chain");

            if (target == null)
                throw new BadRequestException("There is no pulse at the target timestamp in this chain");

            if (anchor == null)
                throw new BadRequestException("There is no pulse at the anchor timestamp in this chain");

            return skiplist(anchor.getPulseIndex(), target.getPulseIndex(), offset,limit,chain);

    }

    @Transactional(readOnly = true)
    public PagedResponseDto skiplist(Long anchorId, Long targetId, int offset, int limit,long chain){

        PulseEntity anchor  = pulsesRepository.findByChainAndPulseIndex(chain,anchorId);
        PulseEntity target = pulsesRepository.findByChainAndPulseIndex(chain, targetId);

        if(anchor == null && target == null)
            throw new BadRequestException("There is no pulse at searched pulse ids and chain");

        if(target == null)
            throw new BadRequestException("There is no pulse at the target pulse id and chain");

        if(anchor == null)
            throw new BadRequestException("There is no pulse at the anchor pulse id and chain");

        if(anchorId<targetId)
            throw new BadRequestException("anchorId must be greater than targetId");

        ZonedDateTime nextYear =  target.getTimeStamp().with(TemporalAdjusters.firstDayOfNextYear()).withHour(0).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime nextMonth = target.getTimeStamp().with(TemporalAdjusters.firstDayOfNextMonth()).withHour(0).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime nextDay = target.getTimeStamp().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime nextHour = target.getTimeStamp().plusHours(1).withMinute(0).withSecond(0).withNano(0);

        List<ZonedDateTime> firstDaysOfYears = new ArrayList();
        firstDaysOfYears.add(null);
        ZonedDateTime link = target.getTimeStamp().with(TemporalAdjusters.firstDayOfNextYear()).withHour(0).withMinute(0).withSecond(0).withNano(0);

        while(link.isBefore( anchor.getTimeStamp())){
            firstDaysOfYears.add(link);
            link = link.plusYears(1);
        }

        List<PulseDto> dtos = new ArrayList<>();
        List<PulseEntity> sequence = new ArrayList<>();

        int pageIndex = offset / limit;
        Pageable page = PageRequest.of(pageIndex,limit);

        long countSkipList = pulsesRepository.countSkiplistByChain(anchor.getPulseIndex(),target.getPulseIndex(),nextYear,nextMonth,nextDay,nextHour,firstDaysOfYears, target.getChainIndex());

        sequence.addAll(pulsesRepository.getSkiplistByChainAndIndex(anchor.getPulseIndex(),target.getPulseIndex(),nextYear,nextMonth,nextDay,nextHour, firstDaysOfYears,target.getChainIndex(),page));

        sequence.forEach(entity -> dtos.add(new PulseDto(entity)));

        PagedResponseDto pagedResponseDto = new PagedResponseDto(dtos);

        String version = env.getProperty("beacon.url.version");
        String first = env.getProperty("beacon.url")+"/beacon/"+ version +"/skiplist?anchorId="+ anchor.getPulseIndex()+"&targetId="+target.getPulseIndex()+"&chainId="+target.getChainIndex();


        int previousIndex = pageIndex - 1;
        int nextIndex = pageIndex + 1;

        int lastPage = (int) Math.ceil(countSkipList / limit);

        String previous=countSkipList<limit || previousIndex<0 ?"null":env.getProperty("beacon.url")+"/beacon/"+ version +"/skiplist?anchorId="+ anchor.getPulseIndex()+"&targetId="+target.getPulseIndex()+"&chainId="+target.getChainIndex()+"&offset="+ previousIndex * limit +"&limit="+limit;
        String next= nextIndex*limit > countSkipList ?"null":env.getProperty("beacon.url")+"/beacon/"+ version +"/skiplist?anchorId="+ anchor.getPulseIndex()+"&targetId="+target.getPulseIndex()+"&chainId="+target.getChainIndex()+"&offset="+ nextIndex * limit +"&limit="+limit;

        String last= countSkipList<limit? first: env.getProperty("beacon.url")+"/beacon/"+ version +"/skiplist?anchorId="+ anchor.getPulseIndex()+"&targetId="+target.getPulseIndex()+"&chainId="+target.getChainIndex()+"&offset="+ lastPage* limit +"&limit="+limit;

        Map<String,String> links = new LinkedHashMap<>();
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
