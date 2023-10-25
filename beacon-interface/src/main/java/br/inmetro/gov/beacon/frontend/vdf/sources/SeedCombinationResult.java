package br.inmetro.gov.beacon.frontend.vdf.sources;

import br.inmetro.gov.beacon.frontend.vdf.scheduling.CombinationResultDto;
import org.springframework.stereotype.Component;

@Component
public class SeedCombinationResult implements SeedInterface{

    private static final String DESCRIPTION = "Beacon Combination Result";

    private CombinationResultDto dto;

    public void setCombinationResultDto(CombinationResultDto dto){
        this.dto = dto;
    }

    @Override
    public SeedSourceDto getSeed() {
        SeedSourceDto seedSourceDto = new SeedSourceDto(dto.getTimeStamp(), dto.getUri(),
                dto.getOutputValue(), DESCRIPTION, SeedCombinationResult.class);
        this.dto = null;
        return seedSourceDto;
    }
}
