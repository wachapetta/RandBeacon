package com.example.beacon.vdf.sources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class SeedBuilder {

    private final ApplicationContext context;

    @Autowired
    public SeedBuilder(ApplicationContext context) {
        this.context = context;
    }

//     Beacon Combination
    public List<SeedSourceDto> getPreDefSeedCombination(){
        final List<SeedSourceDto> seedList = new ArrayList<>();
        SeedSourceDto nistSeed = context.getBean(SeedLastNist.class).getSeed();
        SeedSourceDto uChileSeed = context.getBean(SeedLastChile.class).getSeed();
        SeedSourceDto anuSeed = context.getBean(SeedAnuQuantumRNG.class).getSeed();

        if(nistSeed!=null)
            seedList.add(nistSeed);
        if(uChileSeed!=null)
            seedList.add(uChileSeed);
        if(anuSeed != null)
            seedList.add(anuSeed);


        return new ArrayList<>(seedList);
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
