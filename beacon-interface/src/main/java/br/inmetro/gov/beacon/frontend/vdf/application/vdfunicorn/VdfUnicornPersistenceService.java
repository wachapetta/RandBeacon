package br.inmetro.gov.beacon.frontend.vdf.application.vdfunicorn;

import br.inmetro.gov.beacon.frontend.vdf.infra.entity.VdfUnicornEntity;
import br.inmetro.gov.beacon.frontend.vdf.repository.VdfUnicornRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class VdfUnicornPersistenceService {

    private final VdfUnicornRepository vdfUnicornRepository;

    @Autowired
    public VdfUnicornPersistenceService(VdfUnicornRepository vdfUnicornRepository) {
        this.vdfUnicornRepository = vdfUnicornRepository;
    }

    @Transactional
    public void save(VdfUnicornEntity unicornEntity){
        vdfUnicornRepository.save(unicornEntity);
    }

}
