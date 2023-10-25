package br.inmetro.gov.beacon.frontend.vdf.sources;

import br.inmetro.gov.beacon.frontend.vdf.repository.BeaconRemoteDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Component
public class SeedLastNist implements SeedInterface {

    private final RestTemplate restTemplate;

    private static final String DESCRIPTION = "Last precommitment NIST";

    private final Logger log = LoggerFactory.getLogger(SeedLastNist.class);

    @Autowired
    public SeedLastNist(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public SeedSourceDto getSeed() {
//        RestTemplate restTemplate = new RestTemplate();
        try{
            BeaconRemoteDto lastPulse = restTemplate.getForObject("https://beacon.nist.gov/beacon/2.0/pulse/last", BeaconRemoteDto.class);

            return new SeedSourceDto(Instant.now().toString(), lastPulse.getUri(), lastPulse.getPrecommitmentValue(),
                DESCRIPTION, SeedLastNist.class);
        }catch (Exception e){
            log.warn("Nist's beacon not available");
        }
        return null;
    }
}
