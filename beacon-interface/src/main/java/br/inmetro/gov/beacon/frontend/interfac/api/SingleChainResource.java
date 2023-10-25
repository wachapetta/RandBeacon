package br.inmetro.gov.beacon.frontend.interfac.api;

import br.inmetro.gov.beacon.frontend.interfac.api.dto.PulseDto;
import br.inmetro.gov.beacon.frontend.interfac.domain.service.ActiveChainService;
import br.inmetro.gov.beacon.frontend.interfac.domain.service.QuerySinglePulsesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
@RequestMapping(value = {"/beacon/2.0/chain/{chainIndex}/pulse","/beacon/2.1/chain/{chainIndex}/pulse",}, produces= MediaType.APPLICATION_JSON_VALUE)
public class SingleChainResource {

    private SinglePulseResource singlePulseResource;

    private final QuerySinglePulsesService singlePulsesService;
    private final ActiveChainService activeChainService;

    @Autowired
    public SingleChainResource(QuerySinglePulsesService singlePulsesService, ActiveChainService activeChainService, SinglePulseResource singlePulseResource) {
        this.singlePulsesService = singlePulsesService;
        this.activeChainService = activeChainService;
        this.singlePulseResource = singlePulseResource;
    }

    private Long checkChain(String chainIndex) throws NumberFormatException{
        Long index = 1l;

        if(chainIndex.equals("last")){
            return activeChainService.get().getChainIndex();
        }

        index = Long.parseLong(chainIndex);

        return index;
    }

    @GetMapping("/first")
    @ResponseBody
    public ResponseEntity first(@PathVariable String chainIndex){

        Long index = 1l;

        try {
            index = checkChain(chainIndex);
        }
        catch (NumberFormatException nfe){
            return ResourceResponseUtil.invalidCall();
        }

        PulseDto pulseDto = singlePulsesService.firstDto(index);

        if (pulseDto==null){
            return ResourceResponseUtil.pulseNotAvailable();
        }

        return new ResponseEntity(pulseDto, HttpStatus.OK);
    }

    @GetMapping(value = {"/last"})
    @ResponseBody
    public ResponseEntity last(@PathVariable String chainIndex){

        Long index = 1l;

        try {
            index = checkChain(chainIndex);
            PulseDto pulseDto = singlePulsesService.lastDto(index);

            if (pulseDto==null){
                return ResourceResponseUtil.pulseNotAvailable();
            }
            return new ResponseEntity(pulseDto, HttpStatus.OK);

        }
        catch (NumberFormatException nfe){
            return ResourceResponseUtil.invalidCall();
        }
        catch (Exception e){
            return ResourceResponseUtil.badRequest();
        }
    }

    @GetMapping("/{pulseIndex}")
    @ResponseBody
    public ResponseEntity chainAndPulse(@PathVariable String chainIndex, @PathVariable Long pulseIndex){
        Long index = 1l;

        try {
            index = checkChain(chainIndex);
            PulseDto pulseDto = singlePulsesService.findByChainAndPulseId(index, pulseIndex);

            if (pulseDto==null){
                return ResourceResponseUtil.pulseNotAvailable();
            }

            return new ResponseEntity(pulseDto, HttpStatus.OK);
        }
        catch (NumberFormatException nfe){
            return ResourceResponseUtil.invalidCall();
        }
        catch (Exception e){
            return ResourceResponseUtil.badRequest();
        }
    }

    @GetMapping(value = {"","/"})
    @ResponseBody
    public ResponseEntity get(@RequestParam Map<String,String> allParams,@PathVariable String chainIndex){

        if(allParams.containsKey("timeGT")){
            return singlePulseResource.next(allParams.get("timeGT"));
        }
        else if(allParams.containsKey("timeLT")){
            return singlePulseResource.previous(allParams.get("timeLT"));
        }
        else if(allParams.containsKey("timeGE")) {
            ResponseEntity equals = singlePulseResource.specificTime(allParams.get("timeGE"));
            if(equals.getStatusCode() == HttpStatus.OK)
                return equals;

            return singlePulseResource.next(allParams.get("timeGE"));
        }
        else if(allParams.containsKey("timeLE")) {
            ResponseEntity equals = singlePulseResource.specificTime(allParams.get("timeLE"));
            if(equals.getStatusCode() == HttpStatus.OK)
                return equals;

            return singlePulseResource.previous(allParams.get("timeLE"));

        }else if(allParams.containsKey("outputValue")) {

            return ResourceResponseUtil.notImplemented();
        }
        else if(allParams.containsKey("precommitmentValue")) {

            return ResourceResponseUtil.notImplemented();
        }
        else if(allParams.containsKey("localRandomValue")) {

            return ResourceResponseUtil.notImplemented();
        }
        else if(allParams.containsKey("certificateId")) {

            return ResourceResponseUtil.notImplemented();
        }else if(allParams.containsKey("use")) {

            if(allParams.get("use").equals("first")){
                return first(chainIndex);
            }
        }

        return last(chainIndex);
    }

}
