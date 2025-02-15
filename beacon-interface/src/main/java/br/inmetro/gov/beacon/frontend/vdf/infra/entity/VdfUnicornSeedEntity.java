package br.inmetro.gov.beacon.frontend.vdf.infra.entity;

import br.inmetro.gov.beacon.frontend.vdf.application.combination.dto.SeedUnicordCombinationVo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@Table(name = "vdf_unicorn_seed")
@NoArgsConstructor
@AllArgsConstructor
public class VdfUnicornSeedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String seed;

    private String description;

    private String uri;

    private String cumulativeHash;

    private java.time.ZonedDateTime timeStamp;

    private int seedIndex;

    @ManyToOne
    @JoinColumn(name = "vdf_unicorn_id")
    private VdfUnicornEntity vdfUnicornEntity;

    public VdfUnicornSeedEntity(SeedUnicordCombinationVo dto, VdfUnicornEntity vdfUnicornEntity) {
        this.seed = dto.getSeed();
        this.description = dto.getDescription();
        this.uri = dto.getUri();
        this.cumulativeHash = dto.getCumulativeHash();
        this.timeStamp = dto.getTimeStamp();
        this.vdfUnicornEntity = vdfUnicornEntity;
    }
}
