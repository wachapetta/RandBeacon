package com.example.beacon.vdf.application.combination;

import br.gov.inmetro.beacon.library.aspects.TimingPerformanceAspect;
import br.gov.inmetro.beacon.library.ciphersuite.suite0.ICipherSuite;
import br.gov.inmetro.beacon.library.ciphersuite.suite0.CipherSuiteBuilder;
import com.example.beacon.vdf.application.combination.dto.SeedUnicordCombinationVo;
import com.example.beacon.vdf.infra.entity.CombinationEntity;
import com.example.beacon.vdf.sources.SeedBuilder;
import com.example.beacon.vdf.sources.SeedSourceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.beacon.vdf.infra.util.DateUtil.getCurrentTrucatedZonedDateTime;
import static java.lang.Thread.sleep;

@Service
public class CombinationService {

    private final ICipherSuite cipherSuite;

    private final SeedBuilder seedBuilder;

    private List<SeedUnicordCombinationVo> seedUnicordCombinationVos = new ArrayList<>();

    private final CombinationCalcAndPersistService combinationCalcAndPersistService;

    private static final Logger logger = LoggerFactory.getLogger(CombinationService.class);

    private final int iterations;

    private final int countLimit;

    @Autowired
    public CombinationService(Environment env, SeedBuilder seedBuilder, CombinationCalcAndPersistService combinationCalcAndPersistService) {
        this.seedBuilder = seedBuilder;
        this.combinationCalcAndPersistService = combinationCalcAndPersistService;
        this.cipherSuite = CipherSuiteBuilder.build(0);
        this.iterations = Integer.parseInt(env.getProperty("beacon.combination.iterations"));
        this.countLimit = Integer.parseInt(env.getProperty("beacon.combination.sources.seconds-to-retry"));
    }

    @TimingPerformanceAspect
    public void run() throws Exception {
        logger.warn("Start run:");

        ZonedDateTime now = getCurrentTrucatedZonedDateTime();
        List<SeedSourceDto> preDefSeedCombination = seedBuilder.getPreDefSeedCombination(now);

        if (preDefSeedCombination.isEmpty()) return;

        List<SeedSourceDto> seeds = new ArrayList<>();
        seeds.addAll(preDefSeedCombination);
        seeds.addAll(seedBuilder.getHonestPartyCombination());

        seedUnicordCombinationVos = new CombinationUncornCumulativeHash().calcSeedConcat(cipherSuite, seeds);

        final BigInteger x = new BigInteger(seedUnicordCombinationVos.get(seedUnicordCombinationVos.size() - 1).getCumulativeHash(), 16);

        runAndPersist(x);
    }


    private List<SeedSourceDto> getDelayedPulses(){
        try {
            sleep(200); // one second
        } catch (InterruptedException e) {
            logger.warn("interrupted");
        }
        return seedBuilder.getPreDefSeedCombination(ZonedDateTime.now());
    }

    private void runAndPersist(BigInteger x) throws Exception {

        BigInteger y = combinationCalcAndPersistService.run( x, iterations);

        CombinationEntity combinationEntity = combinationCalcAndPersistService.createCombinationEntity(seedUnicordCombinationVos, iterations, x, y);

        combinationCalcAndPersistService.save(combinationEntity);
    }

}

