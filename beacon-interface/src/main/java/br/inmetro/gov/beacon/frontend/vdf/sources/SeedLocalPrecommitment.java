package br.inmetro.gov.beacon.frontend.vdf.sources;

import br.inmetro.gov.beacon.frontend.vdf.scheduling.PrecommitmentQueueDto;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class SeedLocalPrecommitment implements SeedInterface{

    private static final String DESCRIPTION = "Local Precommitment";

    private PrecommitmentQueueDto dto;

    public void setPrecommitment(PrecommitmentQueueDto dto){
        this.dto = dto;
    }

    @Override
    public SeedSourceDto getSeed() {
        SeedSourceDto seedSourceDto = new SeedSourceDto(Instant.now().toString(), dto.getUri(),
                dto.getPrecommitment(), DESCRIPTION, SeedLocalPrecommitment.class);
        this.dto = null;
        return seedSourceDto;
    }
}
