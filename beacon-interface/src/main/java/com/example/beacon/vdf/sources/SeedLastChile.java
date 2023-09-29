package com.example.beacon.vdf.sources;

import com.example.beacon.vdf.repository.BeaconRemoteDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SeedLastChile implements SeedInterface {

    private final RestTemplate restTemplate;

    private static final String DESCRIPTION = "Last precommitment Chile";

    private final Logger log = LoggerFactory.getLogger(SeedLastChile.class);

    @Autowired
    public SeedLastChile(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public SeedSourceDto getSeed() {
//        RestTemplate restTemplate = new RestTemplate();
        try {
            BeaconRemoteDto lastPulse = restTemplate.getForObject("https://random.uchile.cl/beacon/2.0-beta1/pulse?last", BeaconRemoteDto.class);
            return new SeedSourceDto(lastPulse.getTimeStamp(), lastPulse.getUri(),
                    lastPulse.getPrecommitmentValue(), DESCRIPTION, SeedLastChile.class);
        }catch (Exception e ){
            log.warn("UChile's beacon not available");
        }
        return null;
    }
}
