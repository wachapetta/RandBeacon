package com.example.beacon.vdf.sources;


import com.example.beacon.vdf.repository.DrandRemoteDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Component
public class SeedDrandRNG implements SeedInterface {

    private final RestTemplate restTemplate;

    private static final String DESCRIPTION = "Drand's random bytes";

    private final Logger log = LoggerFactory.getLogger(SeedDrandRNG.class);

    @Autowired
    public SeedDrandRNG(RestTemplate restTemplate, Environment env){

        this.restTemplate = restTemplate;
    }

    @Override
    public SeedSourceDto getSeed() {
        try {
            DrandRemoteDto lastPulse = restTemplate.getForObject("https://api3.drand.sh/8990e7a9aaed2ffed73dbd7092123d6f289930540d7651336225dc172e51b2ce/public/latest", DrandRemoteDto.class);
            return new SeedSourceDto(Instant.now().toString(), "https://api3.drand.sh/8990e7a9aaed2ffed73dbd7092123d6f289930540d7651336225dc172e51b2ce/public/{}".replace("{}",lastPulse.getRound()),
                    lastPulse.getRandomness(), DESCRIPTION, SeedDrandRNG.class);
        }catch (Exception e ){
            log.warn("Drand Service not available");
        }
        return null;
    }
}