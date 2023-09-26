package com.example.beacon.vdf.application.combination;

import com.example.beacon.interfac.api.ResourceResponseUtil;
import com.example.beacon.interfac.domain.pulse.ExternalDto;
import com.example.beacon.vdf.application.VdfPulseDto;
import com.example.beacon.vdf.application.VdfSeedDto;
import com.example.beacon.vdf.application.combination.dto.VdfSlothDto;
import com.example.beacon.vdf.infra.entity.CombinationEntity;
import com.example.beacon.vdf.infra.util.DateUtil;
import com.example.beacon.vdf.repository.CombinationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
@RequestMapping(value = "/beacon/2.0/combination/", produces= MediaType.APPLICATION_JSON_VALUE)
public class CombinationResource {

    private final CombinationRepository combinationRepository;

    public CombinationResource(CombinationRepository combinationRepository) {
        this.combinationRepository = combinationRepository;
    }

    @GetMapping("pulse/{pulseIndex}")
    public ResponseEntity byPulseIndex(@PathVariable String pulseIndex) {
        try {
            CombinationEntity byPulseIndex = combinationRepository.findByPulseIndex(Long.parseLong(pulseIndex));
            if (byPulseIndex == null) {
                return ResourceResponseUtil.pulseNotAvailable();
            }
            return new ResponseEntity(convertToDto(byPulseIndex), HttpStatus.OK);
        }
        catch (RuntimeException e){
            return ResourceResponseUtil.invalidCall();
        }
        catch (Exception e){
            return ResourceResponseUtil.internalError();
        }
    }

    @GetMapping("time/{timeStamp}")
    @ResponseBody
    public ResponseEntity specificTime(@PathVariable String timeStamp){
        try {
            ZonedDateTime zonedDateTime = DateUtil.longToLocalDateTime(timeStamp);
            CombinationEntity byTimeStamp = combinationRepository.findByTimeStamp(zonedDateTime);

            if (byTimeStamp==null){
                return ResourceResponseUtil.pulseNotAvailable();
            }
            return new ResponseEntity(convertToDto(byTimeStamp), HttpStatus.OK);

        } catch (RuntimeException e){
            return ResourceResponseUtil.invalidCall();

        } catch (Exception e){
            return ResourceResponseUtil.internalError();
        }
    }

    @GetMapping("/first")
    @ResponseBody
    public ResponseEntity first(){

        Long first = combinationRepository.findFirst();
        CombinationEntity byPulseIndex = combinationRepository.findByPulseIndex(first);

        if (byPulseIndex==null){
            return ResourceResponseUtil.pulseNotAvailable();
        }

        return new ResponseEntity(convertToDto(byPulseIndex), HttpStatus.OK);
    }

    @GetMapping("/next/{timeStamp}")
    @ResponseBody
    public ResponseEntity next(@PathVariable String timeStamp){
        try {
            ZonedDateTime zonedDateTime = DateUtil.longToLocalDateTime(timeStamp);
            CombinationEntity next = combinationRepository.findNext(zonedDateTime);

            if (next==null){
                return ResourceResponseUtil.pulseNotAvailable();
            }

            CombinationEntity byTimeStamp = combinationRepository.findByTimeStamp(next.getTimeStamp());
            return new ResponseEntity(convertToDto(byTimeStamp), HttpStatus.OK);

        } catch (RuntimeException e){
            return ResourceResponseUtil.invalidCall();
        } catch (Exception e){
            return ResourceResponseUtil.internalError();
        }
    }

    @GetMapping("/previous/{timeStamp}")
    public ResponseEntity previous(@PathVariable String timeStamp){
        try {
            ZonedDateTime zonedDateTime = DateUtil.longToLocalDateTime(timeStamp);
            CombinationEntity previous = combinationRepository.findPrevious(zonedDateTime);

            if (previous==null){
                return ResourceResponseUtil.pulseNotAvailable();
            }

            CombinationEntity byTimeStamp = combinationRepository.findByTimeStamp(previous.getTimeStamp());
            return new ResponseEntity(convertToDto(byTimeStamp), HttpStatus.OK);

        } catch (RuntimeException e){
            return ResourceResponseUtil.invalidCall();
        } catch (Exception e){
            return ResourceResponseUtil.internalError();
        }

    }

    @GetMapping(value = {"/last","","/","pulse"})
    @ResponseBody
    public ResponseEntity last(){
        try {

            Long maxId = combinationRepository.findMaxPulseIndex();

            CombinationEntity byPulseIndex = combinationRepository.findByPulseIndex(maxId);

            if (byPulseIndex==null){
                return ResourceResponseUtil.pulseNotAvailable();
            }
            return new ResponseEntity(convertToDto(byPulseIndex), HttpStatus.OK);

        } catch (RuntimeException e){
            return ResourceResponseUtil.invalidCall();
        } catch (Exception e){
            return ResourceResponseUtil.internalError();
        }
    }

    private VdfPulseDto convertToDto(CombinationEntity entity){

        VdfPulseDto dto = new VdfPulseDto();

        dto.setUri(entity.getUri());
        dto.setVersion(entity.getVersion());
        dto.setCertificateId(entity.getCertificateId());
        dto.setCipherSuite(entity.getCipherSuite());
        dto.setPulseIndex(entity.getPulseIndex());
        dto.setTimeStamp(DateUtil.getTimeStampFormated(entity.getTimeStamp()));
        dto.setSignatureValue(entity.getSignatureValue());
        dto.setPeriod(entity.getPeriod());
        dto.setCombination(entity.getCombination());
        dto.setOutputValue(entity.getOutputValue());

        dto.setExternal(ExternalDto.newExternalFromEntity(entity.getExternal()));

        entity.getSeedList().forEach(s ->
                                dto.addSeed(new VdfSeedDto(s.getSeed(),
                                DateUtil.getTimeStampFormated(s.getTimeStamp()),
                                s.getDescription(), s.getUri(),
                                s.getCumulativeHash())));

        dto.setSlothDto(new VdfSlothDto(entity.getP(), entity.getX(), entity.getIterations(), entity.getY()));

        return dto;
    }

}
