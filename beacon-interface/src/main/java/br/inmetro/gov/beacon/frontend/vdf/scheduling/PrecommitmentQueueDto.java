package br.inmetro.gov.beacon.frontend.vdf.scheduling;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
    DTO used to be transmitted via rabbitmq
 */
@Data @NoArgsConstructor @AllArgsConstructor
public class PrecommitmentQueueDto {
    private String timeStamp;
    private String precommitment;
    private String uri;
}
