package br.inmetro.gov.beacon.frontend.interfac.api.dto;

import br.inmetro.gov.beacon.frontend.interfac.domain.pulse.ExternalDto;
import br.inmetro.gov.beacon.frontend.interfac.domain.pulse.ListValue;
import br.inmetro.gov.beacon.frontend.interfac.infra.PulseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonTypeName("pulse")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT ,use = JsonTypeInfo.Id.NAME)
public class PulseDto {

    private String uri;

    private String version;

    private int cipherSuite;

    private int period;

    private String certificateId;

    private long chainIndex;

    private long pulseIndex;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSz")
    private java.time.ZonedDateTime timeStamp;

    private String localRandomValue;

    private ExternalDto external;

    private List<ListValue> listValues;

    private String precommitmentValue;

    private int statusCode;

    private String signatureValue;

    private String outputValue;

    public PulseDto() {
    }

    public PulseDto(PulseEntity pulseEntity){
        this.pulseIndex = pulseEntity.getPulseIndex();
        this.period = pulseEntity.getPeriod();
        this.timeStamp = pulseEntity.getTimeStamp().withZoneSameInstant((ZoneOffset.UTC).normalized());
        this.uri = pulseEntity.getUri();
        this.version = pulseEntity.getVersion();
        this.certificateId = pulseEntity.getCertificateId();
        this.chainIndex = pulseEntity.getChainIndex();
        this.localRandomValue = pulseEntity.getLocalRandomValue();
        this.precommitmentValue = pulseEntity.getPrecommitmentValue();
        this.signatureValue = pulseEntity.getSignatureValue();
        this.outputValue = pulseEntity.getOutputValue();
        this.statusCode = pulseEntity.getStatusCode();

        this.external =  ExternalDto.newExternalFromEntity(pulseEntity.getExternalEntity());
        this.listValues  = convertListValuesToPulse(pulseEntity);
    }

    private static List<ListValue> convertListValuesToPulse(PulseEntity pulseEntity){
        List<ListValue> listValues = new ArrayList<>();
        pulseEntity.getListValueEntities().forEach(
                entity -> listValues.add(ListValue.getOneValue(entity.getValue(), entity.getType(), entity.getUri())));
        return listValues;
    }

}
