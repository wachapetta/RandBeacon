package com.example.beacon.interfac.api;

import com.example.beacon.interfac.domain.service.ActiveChainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.beacon.interfac.api.dto.PulseDto;
import com.example.beacon.interfac.domain.service.QuerySinglePulsesService;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
@RequestMapping(value = "/beacon/2.0/chain/{chainIndex}/pulse", produces= MediaType.APPLICATION_JSON_VALUE)
public class SingleChainResource {

    private final QuerySinglePulsesService singlePulsesService;
    private final ActiveChainService activeChainService;

    @Autowired
    public SingleChainResource(QuerySinglePulsesService singlePulsesService, ActiveChainService activeChainService) {
        this.singlePulsesService = singlePulsesService;
        this.activeChainService = activeChainService;
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

    @GetMapping(value = {"/last","","/"})
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

}
