package com.example.beacon.interfac.domain.service;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.beacon.interfac.api.dto.PulseDto;
import com.example.beacon.interfac.domain.repository.PulsesRepository;
import com.example.beacon.interfac.infra.PulseEntity;

@Service
public class QuerySinglePulsesService {

    private final PulsesRepository pulsesRepository;

    private final ActiveChainService activeChainService;

    @Autowired
    public QuerySinglePulsesService(PulsesRepository records, ActiveChainService activeChainService) {
        this.pulsesRepository = records;
        this.activeChainService = activeChainService;
    }

    public Optional<PulseEntity> last(Long chain) {
        return Optional.ofNullable(pulsesRepository.last(chain));
    }

    public PulseDto lastDto(Long chain) {
        PulseEntity last = pulsesRepository.last(chain);
        if (last==null){
            return null;
        } else {
            return new PulseDto(last);
        }
    }

    public PulseDto firstDto(Long chain) {
        PulseEntity last = pulsesRepository.first(chain);
        if (last==null){
            return null;
        } else {
            return new PulseDto(last);
        }
    }

    public PulseDto findSpecificTime(ZonedDateTime specificTimeStamp){
        PulseEntity byTimestamp = pulsesRepository.findByTimestamp(specificTimeStamp);
        if (byTimestamp==null){

            PulseEntity next = pulsesRepository.findNext(specificTimeStamp);
            PulseEntity previous = pulsesRepository.findPrevious(specificTimeStamp);

            long between1 = 0; long between2 = 0;

            if (next != null){
                between1 = ChronoUnit.MINUTES.between(specificTimeStamp, next.getTimeStamp());
            }

            if (previous != null){
                between2 = ChronoUnit.MINUTES.between(previous.getTimeStamp(), specificTimeStamp);
            }

            if (between1==0 && between2==0){
                return null;
            }

            if (between1 <= between2){
                return new PulseDto(pulsesRepository.findByTimestamp(previous.getTimeStamp()));
            } else {
                return new PulseDto(pulsesRepository.findByTimestamp(next.getTimeStamp()));
            }

        } else {
            return new PulseDto(pulsesRepository.findByTimestamp(byTimestamp.getTimeStamp()));
        }
    }

    public PulseDto findNext(ZonedDateTime timeStamp){
        PulseEntity next = pulsesRepository.findNext(timeStamp);
        if (next==null){
            return null;
        } else {
            return new PulseDto(pulsesRepository.findByTimestamp(next.getTimeStamp()));
        }
    }

    public PulseDto findPrevious(ZonedDateTime timeStamp){
        PulseEntity next = pulsesRepository.findPrevious(timeStamp);
        if (next==null){
            return null;
        } else {
            return new PulseDto(pulsesRepository.findByTimestamp(next.getTimeStamp()));
        }
    }


    public PulseDto findLast() {
        PulseEntity last = pulsesRepository.last(activeChainService.get().getChainIndex());

        if (last==null){
            return null;
        } else {
            return new PulseDto(last);
        }

    }

    public PulseDto findByChainAndPulseId(Long chainIndex, Long pulseIdx){
        PulseEntity last = pulsesRepository.findByChainAndPulseIndex(chainIndex, pulseIdx);

        if (last == null){
            return null;
        } else {
            return new PulseDto(last);
        }
    }

}