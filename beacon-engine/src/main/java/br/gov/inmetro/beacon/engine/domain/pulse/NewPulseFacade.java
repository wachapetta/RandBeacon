package br.gov.inmetro.beacon.engine.domain.pulse;

import br.gov.inmetro.beacon.engine.domain.repository.PulsesRepository;
import br.gov.inmetro.beacon.engine.infra.PulseEntity;
import br.gov.inmetro.beacon.engine.queue.BeaconVdfQueueSender;
import br.gov.inmetro.beacon.engine.queue.PrecommitmentQueueDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Optional;

import static br.gov.inmetro.beacon.engine.infra.util.DateUtil.getTimeStampFormated;

@Slf4j
@Component
public class NewPulseFacade {

    private final boolean isSend;

    private final BeaconVdfQueueSender beaconVdfQueueSender;

    private final PulsesRepository pulsesRepository;

    @Autowired
    public NewPulseFacade(Environment env, BeaconVdfQueueSender beaconVdfQueueSender, PulsesRepository pulsesRepository){
        this.isSend = Boolean.parseBoolean(env.getProperty("beacon.vdf.combination.send.precom-to-queue"));
        this.beaconVdfQueueSender =beaconVdfQueueSender;
        this.pulsesRepository = pulsesRepository;
    }

    @Async
    public void sendPulseForCombinationQueue(Pulse pulse) throws InterruptedException{

        if (isSend){
            beaconVdfQueueSender.sendCombination(new PrecommitmentQueueDto(getTimeStampFormated(pulse.getTimeStamp()), pulse.getPrecommitmentValue(), pulse.getUri()));
            log.warn("sended to combination");
        }
    }

    @Transactional
    public void save(PulseEntity pulse){
        this.pulsesRepository.save(pulse);
    }

    @Transactional
    public Optional<PulseEntity> findByChainAndTimestamp(long chainIndex, ZonedDateTime timeStamp){
        return pulsesRepository.findByChainAndTimestamp(chainIndex, timeStamp);
    }

    @Transactional
    public PulseEntity getLast(Long chainIndex){
        return pulsesRepository.last(chainIndex);
    }
}
