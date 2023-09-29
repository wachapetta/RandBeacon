package com.example.beacon.vdf.application.vdfunicorn;

import com.example.beacon.vdf.infra.entity.VdfUnicornEntity;
import com.example.beacon.vdf.repository.VdfUnicornRepository;
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
