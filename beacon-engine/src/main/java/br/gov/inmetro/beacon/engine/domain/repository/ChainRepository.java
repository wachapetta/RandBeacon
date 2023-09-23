package br.gov.inmetro.beacon.engine.domain.repository;

import br.gov.inmetro.beacon.engine.infra.ChainEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;

@Repository
public interface ChainRepository extends JpaRepository<ChainEntity, Integer> {
    ChainEntity findTop1ByActive(boolean active);

    @Query(value = "select count(c.id) from ChainEntity c where c.active > 0")
    int numberOfActiveChains();
}
