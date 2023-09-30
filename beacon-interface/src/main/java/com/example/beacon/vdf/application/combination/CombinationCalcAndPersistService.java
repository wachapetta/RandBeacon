package com.example.beacon.vdf.application.combination;

import br.gov.inmetro.beacon.library.ciphersuite.suite0.CipherSuiteBuilder;
import br.gov.inmetro.beacon.library.ciphersuite.suite0.CriptoUtilService;
import br.gov.inmetro.beacon.library.ciphersuite.suite0.ICipherSuite;
import com.example.beacon.interfac.infra.ExternalEntity;
import com.example.beacon.shared.ByteSerializationFields;
import com.example.beacon.vdf.VdfSloth;
import com.example.beacon.vdf.application.combination.dto.SeedUnicordCombinationVo;
import com.example.beacon.vdf.application.vdfunicorn.VdfUnicornService;
import com.example.beacon.vdf.infra.entity.CombinationEntity;
import com.example.beacon.vdf.infra.entity.CombinationSeedEntity;
import com.example.beacon.vdf.repository.CombinationRepository;
import com.example.beacon.vdf.scheduling.CombinationResultDto;
import com.example.beacon.vdf.sources.SeedCombinationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class CombinationCalcAndPersistService {
    private final CombinationRepository combinationRepository;
    private final String certificateId = "04c5dc3b40d25294c55f9bc2496fd4fe9340c1308cd073900014e6c214933c7f7737227fc5e4527298b9e95a67ad92e0310b37a77557a10518ced0ce1743e132";
    private final ICipherSuite cipherSuite;
    private final VdfUnicornService vdfUnicornService;
    private final Environment env;
    private final SeedCombinationResult seedCombinationResult;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();


    private int delayInSeconds;
    private int delayInMinutes;

    @Autowired
    public CombinationCalcAndPersistService(CombinationRepository combinationRepository,
                                            VdfUnicornService vdfUnicornService,
                                            Environment env, SeedCombinationResult seedCombinationResult) {
        this.combinationRepository = combinationRepository;
        this.cipherSuite = CipherSuiteBuilder.build(0);
        this.vdfUnicornService = vdfUnicornService;
        this.env = env;
        this.seedCombinationResult = seedCombinationResult;

        try {
            String[] split = env.getProperty("beacon.combination.delay").split(":");

            this.delayInSeconds = Integer.parseInt(split[1]);
            this.delayInMinutes = Integer.parseInt(split[0]);
        }catch (RuntimeException e ){
            this.delayInSeconds =0;
            this.delayInMinutes =0;
        }
    }

    //@Async("threadPoolTaskExecutor")
    public BigInteger run(BigInteger x, int iterations) throws Exception {


        log.warn("Start combination sloth");
        BigInteger y = VdfSloth.mod_op(x, iterations);
        log.warn("End combination sloth");

        return y;
    }

    @Transactional
    protected void save(CombinationEntity combinationEntity) throws Exception {

        combinationRepository.save(combinationEntity);

        executor.submit(() -> {
            CombinationResultDto combinationResultDto = new CombinationResultDto(combinationEntity.getTimeStamp().toString(), combinationEntity.getOutputValue(), combinationEntity.getUri());
            sendToUnicorn(combinationResultDto);
                    return 0;
                }
        );
        log.warn("Stop run:");
    }

    public CombinationEntity createCombinationEntity(List<SeedUnicordCombinationVo> seedUnicordCombinationVos, int iterations, BigInteger x, BigInteger y) throws Exception {
        Long maxPulseIndex = combinationRepository.findMaxPulseIndex();
        PrivateKey privateKey = CriptoUtilService.loadPrivateKeyPkcs1(env.getProperty("beacon.x509.privatekey"));

        if (maxPulseIndex==null){
            maxPulseIndex = 1L;
        } else {
            maxPulseIndex = maxPulseIndex + 1L ;
        }

        String uri = env.getProperty("beacon.url") +  "/beacon/2.0/combination/pulse/" + maxPulseIndex;

        CombinationEntity combinationEntity = new CombinationEntity();
        combinationEntity.setUri(uri);
        combinationEntity.setVersion("Version 1.0");
        combinationEntity.setPulseIndex(maxPulseIndex);
        combinationEntity.setTimeStamp(ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")).plusMinutes(delayInMinutes).withSecond(delayInSeconds).withNano(0));
        combinationEntity.setCertificateId(this.certificateId);
        combinationEntity.setCipherSuite(0);

        combinationEntity.setExternal(ExternalEntity.newExternalEntity());

        combinationEntity.setCombination(env.getProperty("vdf.combination").toUpperCase());
        combinationEntity.setPeriod(Integer.parseInt(env.getProperty("beacon.combination.period")));

        seedUnicordCombinationVos.forEach(dto ->
                combinationEntity.addSeed(new CombinationSeedEntity(dto, combinationEntity)));

        combinationEntity.setP("9325099249067051137110237972241325094526304716592954055103859972916682236180445434121127711536890366634971622095209473411013065021251467835799907856202363");
        combinationEntity.setX(x.toString());
        combinationEntity.setY(y.toString());
        combinationEntity.setIterations(iterations);

        combinationEntity.setStatusCode(getStatusCode(combinationEntity));

        //sign
        ByteSerializationFields serialization = new ByteSerializationFields(combinationEntity);
        ByteArrayOutputStream baos = serialization.getBaos();

        String signature = cipherSuite.signPkcs15(privateKey, serialization.getBaos().toByteArray());
        combinationEntity.setSignatureValue(signature);

        //outputvalue
        baos.write(serialization.byteSerializeSig(signature).toByteArray());
        String output = cipherSuite.getDigest(baos.toByteArray());
        combinationEntity.setOutputValue(output);

        return combinationEntity;
    }

    protected void sendToUnicorn(CombinationResultDto combinationResultDto) throws Exception {
        if (!vdfUnicornService.isOpen()){
            return;
        }
        seedCombinationResult.setCombinationResultDto(combinationResultDto);
        vdfUnicornService.endTimeSlot();

    }

    private int getStatusCode(CombinationEntity currentEntity){
        int statusCode =0;

        Long maxId = combinationRepository.findMaxId();

        if(maxId == null) maxId = 0L;

        Optional<CombinationEntity> previewsPulse = combinationRepository.findById(maxId);

        if (previewsPulse.isPresent()){
            ZonedDateTime previewPulseTimeStamp = previewsPulse.get().getTimeStamp();
            ZonedDateTime currentPulseTimesptamp = currentEntity.getTimeStamp();

            long betweenInMinutes = ChronoUnit.MINUTES.between(previewPulseTimeStamp, currentPulseTimesptamp);
            if (betweenInMinutes != 10L){
                statusCode += 2;
            }
        } else {
            statusCode += 1;
        }
        return statusCode;
    }

}
