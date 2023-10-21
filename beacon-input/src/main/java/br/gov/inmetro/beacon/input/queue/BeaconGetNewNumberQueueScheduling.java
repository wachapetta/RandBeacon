package br.gov.inmetro.beacon.input.queue;

import br.gov.inmetro.beacon.input.randomness.application.IEntropyAppService;
import br.gov.inmetro.beacon.input.randomness.domain.EntropyDto;
import br.gov.inmetro.beacon.input.randomness.infra.Entropy;
import br.gov.inmetro.beacon.input.randomness.repository.IEntropyRepository;
import br.gov.inmetro.beacon.library.aspects.TimingPerformanceAspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.util.List;

@Component
@EnableScheduling
public class BeaconGetNewNumberQueueScheduling {

    private final BeaconEntropyQueueSender beaconQueueSender;

    private final IEntropyAppService entropyAppService;

    private final IEntropyRepository entropyRepository;

    private static final Logger logger = LoggerFactory.getLogger(BeaconGetNewNumberQueueScheduling.class);
    private final int anticipationInMillis;

    private static boolean isFirstNumber =true;

    @Autowired
    public BeaconGetNewNumberQueueScheduling(BeaconEntropyQueueSender orderQueueSender,
                                             IEntropyAppService entropyAppService,
                                             IEntropyRepository entropyService, Environment env) {
        String tmp = env.getProperty("beacon.input.anticipation");
        this.beaconQueueSender = orderQueueSender;
        this.entropyAppService = entropyAppService;
        this.entropyRepository = entropyService;

        if(tmp!=null)
            this.anticipationInMillis = Integer.parseInt(tmp);
        else
            this.anticipationInMillis =0;
    }

    @Scheduled(cron = "58 * * * * *")
    @TimingPerformanceAspect
    public void runRegular() throws Exception {

        long howLongSleeping = 999-Instant.now().getLong(ChronoField.MILLI_OF_SECOND)-anticipationInMillis;

        if(isFirstNumber && howLongSleeping< 0){
            logger.warn("too late, skipping first number");// too late, skipping to the next schedule
            return;
        }

        if(howLongSleeping>0)
            Thread.sleep(howLongSleeping);

        EntropyDto noiseDto = entropyAppService.getNoise512Bits();
        Entropy saved = entropyRepository.save(noiseDto);

        try {
            beaconQueueSender.sendRegular(noiseDto);
            logger.warn(noiseDto.toString());
            this.isFirstNumber = false;

        } catch (Exception e){
            entropyRepository.sent(saved.getId(), false);
            e.printStackTrace();
        }

    }

    @Scheduled(cron = "0 * * * * *")
    public void runSync() {
        List<EntropyDto> notSentDto = entropyRepository.getNotSentDto();

        if (notSentDto.isEmpty()) return;

        try {
            beaconQueueSender.sendSync(notSentDto);
            entropyRepository.sentDto(notSentDto);
        } catch (Exception e){
            e.printStackTrace();
        }

    }

}
