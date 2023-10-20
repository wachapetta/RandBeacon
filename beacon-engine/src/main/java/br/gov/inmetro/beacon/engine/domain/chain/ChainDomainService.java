package br.gov.inmetro.beacon.engine.domain.chain;

public class ChainDomainService {

    public static ChainValueObject getActiveChain(){
        return new ChainValueObject("2.1",
                                    "2.1.0",
                                    0,
                                        60000,
                                        2L);
    }

}
