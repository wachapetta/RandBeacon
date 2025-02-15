package br.inmetro.gov.beacon.frontend.vdf.infra.entity;

import br.inmetro.gov.beacon.frontend.vdf.application.combination.dto.SeedUnicordCombinationVo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@Table(name = "vdf_combination_seed")
@NoArgsConstructor
@AllArgsConstructor
public class CombinationSeedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String seed;

    private String description;

    private String uri;

    private String cumulativeHash;

    private int seedIndex;

    private java.time.ZonedDateTime timeStamp;

    @ManyToOne
    @JoinColumn(name = "combination_id")
    private CombinationEntity combinationEntity;

    public CombinationSeedEntity(SeedUnicordCombinationVo dto, CombinationEntity CombinationEntity) {
        this.seed = dto.getSeed();
        this.description = dto.getDescription();
        this.uri = dto.getUri();
        this.cumulativeHash = dto.getCumulativeHash();
        this.timeStamp = dto.getTimeStamp();
        this.combinationEntity = CombinationEntity;
    }
}
