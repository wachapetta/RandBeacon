package br.inmetro.gov.beacon.frontend.vdf.application.combination.dto;

import br.inmetro.gov.beacon.frontend.vdf.application.combination.OriginEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VdfPulseDtoPost {

    private String certificateId;

    private int cipherSuite;

    private String seed;

    @JsonProperty("origin")
    private OriginEnum originEnum;

    private String signatureValue;

}
