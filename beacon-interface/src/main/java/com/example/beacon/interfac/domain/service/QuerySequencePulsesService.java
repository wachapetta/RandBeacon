package com.example.beacon.interfac.domain.service;

import com.example.beacon.interfac.domain.repository.PulsesRepository;
import com.example.beacon.interfac.api.dto.PulseDto;
import com.example.beacon.interfac.infra.PulseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
public class QuerySequencePulsesService {

    private final PulsesRepository pulsesRepository;

    @Autowired
    public QuerySequencePulsesService(PulsesRepository pulsesRepository) {
        this.pulsesRepository = pulsesRepository;
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
    public List<PulseDto> skiplist(ZonedDateTime anchor, ZonedDateTime target){

        ZonedDateTime nextYear =  target.with(TemporalAdjusters.firstDayOfNextYear()).withHour(0).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime nextMonth = target.with(TemporalAdjusters.firstDayOfNextMonth()).withHour(0).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime nextDay = target.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime nextHour = target.plusHours(1).withMinute(0).withSecond(0).withNano(0);

        List<PulseDto> dtos = new ArrayList<>();

        List<PulseEntity> sequence = new ArrayList<PulseEntity>();

        sequence.add(pulsesRepository.findByTimestamp(target));
        sequence.addAll(pulsesRepository.skiplistByMinutes(anchor, target,nextHour));
        sequence.addAll(pulsesRepository.skiplistByHours(anchor, target,nextDay));
        sequence.addAll(pulsesRepository.skiplistByDays(anchor, target,nextMonth));
        sequence.addAll(pulsesRepository.skiplistByMonths(anchor, target,nextYear));
        sequence.addAll(pulsesRepository.skiplistByYears(anchor, target));
        sequence.add(pulsesRepository.findByTimestamp(anchor));

        sequence.forEach(entity -> dtos.add(new PulseDto(entity)));
        return dtos;
    }

}
