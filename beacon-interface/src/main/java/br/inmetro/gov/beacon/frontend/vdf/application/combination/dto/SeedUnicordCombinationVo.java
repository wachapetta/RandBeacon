package br.inmetro.gov.beacon.frontend.vdf.application.combination.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SeedUnicordCombinationVo {
    private final String uri;
    private final String seed;
    private final String description;
    private final String cumulativeHash;
    private final java.time.ZonedDateTime timeStamp;
}
