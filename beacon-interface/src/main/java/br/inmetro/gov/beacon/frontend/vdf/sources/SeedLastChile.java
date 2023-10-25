package br.inmetro.gov.beacon.frontend.vdf.sources;

import br.inmetro.gov.beacon.frontend.vdf.repository.BeaconRemoteDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

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
            BeaconRemoteDto lastPulse = restTemplate.getForObject("https://random.uchile.cl/beacon/2.1-beta/pulse", BeaconRemoteDto.class);
            return new SeedSourceDto(Instant.now().toString(), lastPulse.getUri(),
                    lastPulse.getPrecommitmentValue(), DESCRIPTION, SeedLastChile.class);
        }catch (Exception e ){
            log.warn("UChile's beacon not available");
        }
        return null;
    }
}
