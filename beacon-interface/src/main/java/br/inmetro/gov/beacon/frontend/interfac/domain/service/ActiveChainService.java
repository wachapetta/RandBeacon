package br.inmetro.gov.beacon.frontend.interfac.domain.service;

import br.inmetro.gov.beacon.frontend.interfac.domain.repository.ChainRepository;
import br.inmetro.gov.beacon.frontend.interfac.domain.chain.ChainValueObject;
import br.inmetro.gov.beacon.frontend.interfac.infra.ChainEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActiveChainService {

    private final ChainRepository chainRepository;

    @Autowired
    public ActiveChainService(ChainRepository chainRepository) {
        this.chainRepository = chainRepository;
    }

    public ChainValueObject get(){
        ChainEntity entity = chainRepository.findTop1ByActive(true);
        return new ChainValueObject(entity.getVersionUri(), entity.getVersionPulse(), entity.getCipherSuite(), entity.getPeriod(), entity.getChainIndex());
    }

}
