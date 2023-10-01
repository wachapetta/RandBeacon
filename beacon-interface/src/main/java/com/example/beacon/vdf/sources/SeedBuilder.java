package com.example.beacon.vdf.sources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Component
public class SeedBuilder {

    private final ApplicationContext context;

    @Autowired
    public SeedBuilder(ApplicationContext context) {
        this.context = context;
    }

//     Beacon Combination
    public List<SeedSourceDto> getPreDefSeedCombination(ZonedDateTime zonedDateTime){

        final List<SeedSourceDto> seedList = new ArrayList<>();
        List<SeedInterface> seedSources = new ArrayList<SeedInterface>();
        ExecutorService service;

        seedSources.add(context.getBean(SeedLastNist.class));
        seedSources.add(context.getBean(SeedLastChile.class));
        seedSources.add(context.getBean(SeedAnuQuantumRNG.class));

        service = Executors.newFixedThreadPool(seedSources.size());

        IntStream.range(0, seedSources.size()).forEach(index -> {
            service.submit(() -> {
                Instant starting = Instant.now();
                SeedSourceDto seedDto = seedSources.get(index).getSeed();
                while( seedDto == null || seedDto.timeStamp()== null || seedDto.timeStamp().compareTo(zonedDateTime)<0) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {}

                    if(Instant.now().toEpochMilli()-starting.toEpochMilli()>=5000) return;
                    seedDto = seedSources.get(index).getSeed();
                }
            });
        });


        try {
            service.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {}

        seedSources.forEach(seedSource ->{
            SeedSourceDto seed = seedSource.getSeed();
            if( seed !=null && seed.getSeed()!=null && seed.timeStamp().compareTo(zonedDateTime)>=0 )
                seedList.add(seedSource.getSeed());
        });

        seedList.sort(Comparator.comparingInt(SeedSourceDto::hashCode));

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
