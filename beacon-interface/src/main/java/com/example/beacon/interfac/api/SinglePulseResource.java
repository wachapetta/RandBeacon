package com.example.beacon.interfac.api;

import com.example.beacon.interfac.api.dto.PulseDto;
import com.example.beacon.interfac.domain.service.QuerySinglePulsesService;
import com.example.beacon.vdf.infra.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
@RequestMapping(value = "/beacon/2.0/pulse", produces= MediaType.APPLICATION_JSON_VALUE)
public class SinglePulseResource {

    private final QuerySinglePulsesService singlePulsesService;

    @Autowired
    public SinglePulseResource(QuerySinglePulsesService singlePulsesService) {
        this.singlePulsesService = singlePulsesService;
    }

    @GetMapping("time/{timeStamp}")
    @ResponseBody
    public ResponseEntity specificTime(@PathVariable String timeStamp){
        try {
            ZonedDateTime zonedDateTime = DateUtil.longToLocalDateTime(timeStamp);
            PulseDto byTimestamp = singlePulsesService.findSpecificTime(zonedDateTime);

            if (byTimestamp==null){
                return ResourceResponseUtil.pulseNotAvailable();
            }
            return new ResponseEntity(byTimestamp, HttpStatus.OK);

        } catch (DateTimeParseException e){
            return ResourceResponseUtil.invalidCall();
        } catch (Exception e){
            return ResourceResponseUtil.internalError();
        }
    }

    @GetMapping("time/next/{timeStamp}")
    @ResponseBody
    public ResponseEntity next(@PathVariable String timeStamp){
        try {
            ZonedDateTime zonedDateTime = DateUtil.longToLocalDateTime(timeStamp);
            PulseDto byTimestamp = singlePulsesService.findNext(zonedDateTime);

            if (byTimestamp==null){
                return ResourceResponseUtil.pulseNotAvailable();
            }
            return new ResponseEntity(byTimestamp, HttpStatus.OK);

        } catch (DateTimeParseException e){
            return ResourceResponseUtil.invalidCall();
        } catch (Exception e){
            return ResourceResponseUtil.internalError();
        }
    }

    @GetMapping("time/previous/{timeStamp}")
    public ResponseEntity previous(@PathVariable String timeStamp){
        try {
            ZonedDateTime zonedDateTime = DateUtil.longToLocalDateTime(timeStamp);
            PulseDto byTimestamp = singlePulsesService.findPrevious(zonedDateTime);

            if (byTimestamp==null){
                return ResourceResponseUtil.pulseNotAvailable();
            }
            return new ResponseEntity(byTimestamp, HttpStatus.OK);

        } catch (DateTimeParseException e){
            return ResourceResponseUtil.invalidCall();
        } catch (Exception e){
            return ResourceResponseUtil.internalError();
        }

    }

    @GetMapping(value = {"/last"})
    @ResponseBody
    public ResponseEntity last(){
        try {
            PulseDto byTimestamp = singlePulsesService.findLast();

            if (byTimestamp==null){
                return ResourceResponseUtil.pulseNotAvailable();
            }
            return new ResponseEntity(byTimestamp, HttpStatus.OK);

        } catch (DateTimeParseException e){
            return ResourceResponseUtil.invalidCall();
        } catch (Exception e){
            return ResourceResponseUtil.internalError();
        }
    }


    @GetMapping(value = {"","/"})
    @ResponseBody
    public ResponseEntity get(@RequestParam Map<String,String> allParams){

        if(allParams.containsKey("timeGT")){
            return next(allParams.get("timeGT"));
        }
        else if(allParams.containsKey("timeLT")){
            return previous(allParams.get("timeLT"));
        }
        else if(allParams.containsKey("timeGE")) {
            ResponseEntity equals = specificTime(allParams.get("timeGE"));
            if(equals.getStatusCode() == HttpStatus.OK)
                return equals;

            return next(allParams.get("timeGE"));
        }
        else if(allParams.containsKey("timeLE")) {
            ResponseEntity equals = specificTime(allParams.get("timeLE"));
            if(equals.getStatusCode() == HttpStatus.OK)
                return equals;

            return previous(allParams.get("timeLE"));

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
                return first();
            }
        }

        return last();
    }

    @GetMapping(value = {"/first"})
    @ResponseBody
    private ResponseEntity first() {
        try {
            PulseDto first = singlePulsesService.firstDto(1l);

            if (first==null){
                return ResourceResponseUtil.pulseNotAvailable();
            }
            return new ResponseEntity(first, HttpStatus.OK);

        } catch (DateTimeParseException e){
            return ResourceResponseUtil.invalidCall();
        } catch (Exception e){
            return ResourceResponseUtil.internalError();
        }
    }

}
