package com.example.beacon.vdf.sources;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Component
@Slf4j
public class SeedBuilder {

    public static final int timeoutInMillis = 5000;

    public static final int tries = 5;

    private final ApplicationContext context;

    @Autowired
    public SeedBuilder(ApplicationContext context) {
        this.context = context;
    }

//     Beacon Combination
    public List<SeedSourceDto> getPreDefSeedCombination(ZonedDateTime zonedDateTime){

        final List<SeedSourceDto> seedList = Collections.synchronizedList(new ArrayList<>());
        List<SeedInterface> seedSources = new ArrayList<>();
        ExecutorService service;

        seedSources.add(context.getBean(SeedLastNist.class));
        seedSources.add(context.getBean(SeedLastChile.class));
        seedSources.add(context.getBean(SeedAnuQuantumRNG.class));

        Collections.shuffle(seedSources, new Random());

        service = Executors.newFixedThreadPool(seedSources.size());

        Instant starting = Instant.now();

        IntStream.range(0, seedSources.size()).forEach(index -> {
            service.submit(() -> {
                int tries = 5;

                SeedSourceDto seedDto = seedSources.get(index).getSeed();
                log.warn("1st try thread {} seed {}",index,seedSources.get(index).getClass());
                while( seedDto == null || seedDto.timeStamp()== null || seedDto.timeStamp().compareTo(zonedDateTime)<0) {
                    tries--;

                    if (tries==0) return;

                    if(Instant.now().toEpochMilli()-starting.toEpochMilli()>= timeoutInMillis) return;

                    try {
                        Thread.sleep(800);
                    } catch (InterruptedException e) {}

                    seedDto = seedSources.get(index).getSeed();
                    log.warn("thread {} seed {}",index,seedSources.get(index).getClass());

                }
                if( seedDto !=null && seedDto.getSeed()!=null && seedDto.timeStamp().compareTo(zonedDateTime)>=0 ){

                    if(seedDto.getSeed().equals(""))
                        return;

                    synchronized (seedList){
                        seedList.add(seedDto);
                    }
                }
            });
        });

        try {
            service.awaitTermination(timeoutInMillis/1000, TimeUnit.SECONDS);
            service.shutdown();
        } catch (InterruptedException e) {
            service.shutdownNow();
        }

        return seedList;
    }

    public List<SeedSourceDto> getHonestPartyCombination(){
        final List<SeedSourceDto> seedList = new ArrayList<>();

//        calc(seedList);

        seedList.add(context.getBean(SeedLocalPrecommitment.class).getSeed());
        return Collections.unmodifiableList(new ArrayList<>(seedList));
    }

    // VDF / Unicorn
    public List<SeedSourceDto> getPreDefSeedUnicorn(){
        final List<SeedSourceDto> seedList = new ArrayList<>();
        // do something
        return Collections.unmodifiableList(new ArrayList<>(seedList));
    }

    public List<SeedSourceDto> getHonestPartyUnicorn(){
        final List<SeedSourceDto> seedList = new ArrayList<>();
//        seedList.add(context.getBean(SeedLocalPrecommitmentUnicorn.class).getSeed());
        seedList.add(context.getBean(SeedCombinationResult.class).getSeed());
        return Collections.unmodifiableList(new ArrayList<>(seedList));
    }

}
