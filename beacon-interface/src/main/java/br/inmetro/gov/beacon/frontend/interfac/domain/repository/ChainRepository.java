package br.inmetro.gov.beacon.frontend.interfac.domain.repository;

import br.inmetro.gov.beacon.frontend.interfac.infra.ChainEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChainRepository extends JpaRepository<ChainEntity, Integer> {
    ChainEntity findTop1ByActive(boolean active);
}
