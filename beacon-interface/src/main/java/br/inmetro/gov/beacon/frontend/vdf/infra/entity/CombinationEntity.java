package br.inmetro.gov.beacon.frontend.vdf.infra.entity;

import br.inmetro.gov.beacon.frontend.interfac.infra.ExternalEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "vdf_combination")
@NoArgsConstructor
@AllArgsConstructor
public class CombinationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uri;

    private String version;

    private String certificateId;

    private int cipherSuite;

    private long pulseIndex;

    private ZonedDateTime timeStamp;

    private String signatureValue;

    private int period;

    @Embedded
    private ExternalEntity external;

    private String combination;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "combinationEntity", cascade = CascadeType.ALL)
    private List<CombinationSeedEntity> seedList = new ArrayList<>();

    private String p;

    private String x;

    private String y;

    private int iterations;

    private int statusCode;

    private String outputValue;

    private ZonedDateTime createdAt;

    public void addSeed(CombinationSeedEntity seed){
        this.seedList.add(seed);
    }

    @PrePersist
    public void prePersist() {
        createdAt = ZonedDateTime.now();
    }

}
