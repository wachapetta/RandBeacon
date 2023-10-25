package br.inmetro.gov.beacon.frontend.vdf.application;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VdfSeedDto {
    private String seed;
    private String timeStamp;
    private String description;
    private String uri;
    private String cumulativeHash;
}