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
@Table(name = "vdf_unicorn")
@NoArgsConstructor
@AllArgsConstructor
public class VdfUnicornEntity {

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

    private int statusCode;

    private String combination;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "vdfUnicornEntity", cascade = CascadeType.ALL)
    private List<VdfUnicornSeedEntity> seedList = new ArrayList<>();

    private String p;

    private String x;

    private String y;

    private int iterations;

    private String outputValue;

    private ZonedDateTime createdAt;

    public void addSeed(VdfUnicornSeedEntity seed){
        this.seedList.add(seed);
    }

    @PrePersist
    public void prePersist() {
        createdAt = ZonedDateTime.now();
    }
}
