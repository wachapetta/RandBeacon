package com.example.beacon.vdf.scheduling;

import com.example.beacon.vdf.application.combination.CombinationService;
import com.example.beacon.vdf.sources.SeedLocalPrecommitment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.sql.Time;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(
        value="beacon.combination.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class VdfQueueConsumer {

    private final CombinationService combinationService;

    private final SeedLocalPrecommitment seedLocalPrecommitmentCombination;

    private final Environment env;

    private static final Logger logger = LoggerFactory.getLogger(VdfQueueConsumer.class);

    @Autowired
    public VdfQueueConsumer(CombinationService combinationService, SeedLocalPrecommitment seedLocalPrecommitmentCombination, Environment env) {
        this.combinationService = combinationService;
        this.seedLocalPrecommitmentCombination = seedLocalPrecommitmentCombination;
        this.env = env;
    }

    @RabbitListener(queues = {"pulses_combination_queue"})
    public void receiveCombination(PrecommitmentQueueDto dto) throws Exception {
        ZonedDateTime parse = ZonedDateTime.parse(dto.getTimeStamp(), DateTimeFormatter.ISO_DATE_TIME);
        ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        long between = ChronoUnit.MINUTES.between(parse, now);

        if(ZonedDateTime.now().isAfter(now.plusSeconds(15))) {
            logger.warn("Too late, waiting next pulse:" + dto);
            return;
        }

        if (between==0){
            logger.warn("PrecommitmentQueueDto received:  " + dto);
            seedLocalPrecommitmentCombination.setPrecommitment(dto);
            logger.warn("Start combination: " + ZonedDateTime.now());
            combinationService.run();
            logger.warn(String.format("combination released: %s - iterations: %s",  dto.getTimeStamp(), env.getProperty("beacon.combination.iterations")));
        } else {
            logger.warn("Delayed pulse discarded:" + dto);
        }
    }

//    @RabbitListener(queues = {"pulses_unicorn_queue"})
//    public void receiveUnicorn(PrecommitmentQueueDto dto) throws Exception {
//        ZonedDateTime parse = ZonedDateTime.parse(dto.getTimeStamp(), DateTimeFormatter.ISO_DATE_TIME);
//        ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES);
//
//        long between = ChronoUnit.MINUTES.between(parse, now);
//
//        if (!vdfUnicornService.isOpen()){
//            return;
//        }
//        if (between==0){
//            seedLocalPrecommitmentUnicorn.setPrecommitmentUnicorn(dto);
//            vdfUnicornService.endTimeSlot();
//        }
//
//    }

}
