package br.inmetro.gov.beacon.frontend.vdf.application.vdfunicorn;

import br.gov.inmetro.beacon.library.ciphersuite.suite0.CipherSuiteBuilder;
import br.gov.inmetro.beacon.library.ciphersuite.suite0.CriptoUtilService;
import br.gov.inmetro.beacon.library.ciphersuite.suite0.ICipherSuite;
import br.inmetro.gov.beacon.frontend.interfac.infra.ExternalEntity;
import br.inmetro.gov.beacon.frontend.shared.ByteSerializationFields;
import br.inmetro.gov.beacon.frontend.vdf.VdfSloth;
import br.inmetro.gov.beacon.frontend.vdf.application.VdfSeedDto;
import br.inmetro.gov.beacon.frontend.vdf.application.combination.StatusEnum;
import br.inmetro.gov.beacon.frontend.vdf.application.combination.dto.SeedUnicordCombinationVo;
import br.inmetro.gov.beacon.frontend.vdf.infra.entity.VdfUnicornEntity;
import br.inmetro.gov.beacon.frontend.vdf.infra.entity.VdfUnicornSeedEntity;
import br.inmetro.gov.beacon.frontend.vdf.infra.util.DateUtil;
import br.inmetro.gov.beacon.frontend.vdf.repository.VdfUnicornRepository;
import br.inmetro.gov.beacon.frontend.vdf.sources.SeedBuilder;
import br.inmetro.gov.beacon.frontend.vdf.sources.SeedSourceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class VdfUnicornService {

    private final Environment env;
    private final List<Integer> startList;
    private final VdfSloth sloth;

    private StatusEnum statusEnum;

    private ZonedDateTime windowStart;

    private final List<SeedUnicordCombinationVo> seedListUnicordCombination;

    private final ICipherSuite cipherSuite;

    private final String certificateId = "04c5dc3b40d25294c55f9bc2496fd4fe9340c1308cd073900014e6c214933c7f7737227fc5e4527298b9e95a67ad92e0310b37a77557a10518ced0ce1743e132";

    private final SeedBuilder seedBuilder;

    private final VdfUnicornRepository vdfUnicornRepository;

    VdfUnicornPersistenceService persistenceService;

    private static final Logger logger = LoggerFactory.getLogger(VdfUnicornService.class);

    @Autowired
    public VdfUnicornService(Environment environment, SeedBuilder seedBuilder, VdfUnicornRepository vdfUnicornRepository, VdfUnicornPersistenceService persistenceService,VdfSloth sloth) {

        List<String> tmp = Arrays.asList(environment.getProperty("beacon.unicorn.start.submission").replace(" ","").replace("*","").split(","));

        this.env = environment;
        this.seedBuilder = seedBuilder;
        this.vdfUnicornRepository = vdfUnicornRepository;
        this.statusEnum = StatusEnum.STOPPED;
        this.seedListUnicordCombination = new ArrayList<>();
        this.cipherSuite = CipherSuiteBuilder.build(0);

        this.startList = new ArrayList<Integer>();
        tmp.forEach(str-> startList.add(Integer.parseInt(str)));

        this.windowStart = DateUtil.getTimestampOfNextRun(ZonedDateTime.now(),startList);

        this.persistenceService = persistenceService;
        this.sloth = sloth;
    }

    public void startTimeSlot() {
        this.seedListUnicordCombination.clear();
        this.statusEnum = StatusEnum.OPEN;
        this.windowStart = DateUtil.getCurrentTrucatedZonedDateTime();

        List<SeedSourceDto> preDefinedSeeds = seedBuilder.getPreDefSeedUnicorn();
        preDefinedSeeds.forEach(dto -> {
            this.seedListUnicordCombination.add(
                    calcSeedConcat(new SeedPostDto(dto.getSeed(),
                            dto.getDescription(),
                            dto.getUri()),
                            this.seedListUnicordCombination,
                            windowStart));
        });
    }

    public void addSeed(SeedPostDto dto){
        SeedUnicordCombinationVo seedUnicordCombinationVo = calcSeedConcat(dto, this.seedListUnicordCombination,  ZonedDateTime.now());
        this.seedListUnicordCombination.add(seedUnicordCombinationVo);
    }

    private SeedUnicordCombinationVo calcSeedConcat(SeedPostDto dtoNew, List<SeedUnicordCombinationVo> seedList, ZonedDateTime now) {
        String currentValue = "";
        if (seedList.size() == 0) {
            currentValue = dtoNew.getSeed();
        } else {
            for (SeedUnicordCombinationVo vo: seedList) {
                currentValue = currentValue + vo.getSeed();
            }

            currentValue = currentValue + dtoNew.getSeed();
        }

        logger.warn("Current value: {}", currentValue);
        String cumulativeDigest = cipherSuite.getDigest(currentValue);
        logger.warn("Cumulative hash: {}", cumulativeDigest);
        return new SeedUnicordCombinationVo(dtoNew.getUri(), dtoNew.getSeed(), dtoNew.getDescription(), cumulativeDigest, now);
    }

    @Async("threadPoolTaskExecutor")
    public void proceed() throws Exception {

        //if the window started now, ignore the combination seed and wait the next seed
        if(this.startList.contains(ZonedDateTime.now().getMinute())){
            logger.warn("Ignoring combination pulse in the window start minute");
            return;
        }

        this.statusEnum = StatusEnum.RUNNING;
        List<SeedSourceDto> honestSeeds = seedBuilder.getHonestPartyUnicorn();

        logger.warn("Combination output received");

        honestSeeds.forEach(dto -> {
            this.seedListUnicordCombination.add(
                    calcSeedConcat(new SeedPostDto(dto.getSeed(),
                                    dto.getDescription(),
                                    dto.getUri()),
                            this.seedListUnicordCombination,
                            ZonedDateTime.now()));
        });
        run();
    }

    public boolean isOpen(){
        return this.statusEnum.equals(StatusEnum.OPEN);
    }

    //@Async("threadPoolTaskExecutor")
    protected void run() throws Exception {
        SeedUnicordCombinationVo last = this.seedListUnicordCombination.get(this.seedListUnicordCombination.size() - 1);
        final BigInteger x = new BigInteger(last.getCumulativeHash(), 16);

        int iterations = Integer.parseInt(env.getProperty("beacon.unicorn.iterations"));

        this.statusEnum = StatusEnum.STOPPED;

        logger.warn("Start unicorn sloth:");
        BigInteger y = sloth.mod_op(x, iterations).get();
        logger.warn("End unicorn sloth:");



        persist(y,x, iterations);
        seedListUnicordCombination.clear();
        this.windowStart = DateUtil.getTimestampOfNextRun(ZonedDateTime.now(), this.startList);
    }

    public UnicornCurrentDto getUnicornState(){
        UnicornCurrentDto unicornCurrentDto = new UnicornCurrentDto();
        unicornCurrentDto.setStatusEnum(this.statusEnum.toString());

        if (!this.seedListUnicordCombination.isEmpty()){
            unicornCurrentDto.setCurrentHash(this.seedListUnicordCombination.get(this.seedListUnicordCombination.size() - 1).getCumulativeHash());
        }

        //TODO Verificar o horário
        unicornCurrentDto.setStart(DateUtil.getTimeStampFormated(this.windowStart));

        ZonedDateTime nextRun = DateUtil.getTimestampOfNextRun(ZonedDateTime.now(),this.startList);
        long minutesForNextRun = DateUtil.getMinutesForNextRun(ZonedDateTime.now(), nextRun);
        unicornCurrentDto.setNextRunInMinutes(minutesForNextRun);

        unicornCurrentDto.setEnd(DateUtil.getTimeStampFormated(this.windowStart.plusMinutes(5).plusSeconds(5)));

        this.seedListUnicordCombination.forEach(s ->
                unicornCurrentDto.addSeed(new VdfSeedDto(s.getSeed(),
                        DateUtil.getTimeStampFormated(s.getTimeStamp()),
                        s.getDescription(), s.getUri(),
                        s.getCumulativeHash())));

        return unicornCurrentDto;
    }


    protected void persist(BigInteger y, BigInteger x, int iterations) throws Exception {

        Long maxPulseIndex = vdfUnicornRepository.findMaxId();

        if (maxPulseIndex==null){
            maxPulseIndex = 1L;
        } else {
            maxPulseIndex = maxPulseIndex + 1L ;
        }

        String uri = env.getProperty("beacon.url") +  "/unicorn/beacon/2.0/pulse/" + maxPulseIndex;

        VdfUnicornEntity unicornEntity = new VdfUnicornEntity();
        unicornEntity.setUri(uri);
        unicornEntity.setVersion("Version 1.0");
        unicornEntity.setPulseIndex(maxPulseIndex);
        unicornEntity.setTimeStamp(ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        unicornEntity.setCertificateId(this.certificateId);
        unicornEntity.setCipherSuite(0);
        unicornEntity.setCombination(env.getProperty("vdf.combination").toUpperCase());
        unicornEntity.setPeriod(Integer.parseInt(env.getProperty("beacon.unicorn.period")));

        unicornEntity.setExternal(ExternalEntity.newExternalEntity());

        unicornEntity.setStatusCode(0);

        this.seedListUnicordCombination.forEach(SeedUnicordCombinationVo ->
                unicornEntity.addSeed(new VdfUnicornSeedEntity(SeedUnicordCombinationVo, unicornEntity)));

        unicornEntity.setP("9325099249067051137110237972241325094526304716592954055103859972916682236180445434121127711536890366634971622095209473411013065021251467835799907856202363");
        unicornEntity.setX(x.toString());
        unicornEntity.setY(y.toString());
        unicornEntity.setIterations(iterations);

        //sign
        ByteSerializationFields serialization = new ByteSerializationFields(unicornEntity);
        ByteArrayOutputStream baos = serialization.getBaos();

        PrivateKey privateKey = CriptoUtilService.loadPrivateKeyPkcs1(env.getProperty("beacon.x509.privatekey"));
        String signature = cipherSuite.signPkcs15(privateKey, serialization.getBaos().toByteArray());
        unicornEntity.setSignatureValue(signature);

        //outputvalue
        baos.write(serialization.byteSerializeSig(signature).toByteArray());
        String output = cipherSuite.getDigest(baos.toByteArray());
        unicornEntity.setOutputValue(output);

        this.persistenceService.save(unicornEntity);

    }

}
