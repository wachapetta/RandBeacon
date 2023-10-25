package br.inmetro.gov.beacon.frontend.interfac.domain.repository;

import java.time.ZonedDateTime;

import br.inmetro.gov.beacon.frontend.interfac.infra.PulseEntity;

public interface PulsesQueries {
    PulseEntity last(Long chain);
    PulseEntity first(Long chain);
    PulseEntity findByChainAndPulseIndex(Long chainIndex, Long pulseIndex);
    PulseEntity findByTimestamp(ZonedDateTime timeStamp);
    PulseEntity findByTimestampAndChain(ZonedDateTime timeStamp,Long chain);
}
