package com.example.beacon.interfac.api;

import com.example.beacon.interfac.api.dto.PulseDto;
import com.example.beacon.interfac.api.dto.SkiplistDto;
import com.example.beacon.interfac.domain.service.BadRequestException;
import com.example.beacon.interfac.domain.service.QuerySequencePulsesService;
import com.example.beacon.vdf.infra.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
@RequestMapping(value = "/beacon/2.0/skiplist", produces= MediaType.APPLICATION_JSON_VALUE)
public class SequenceOfPulsesResource {

    private final QuerySequencePulsesService querySequencePulsesService;

    @Autowired
    public SequenceOfPulsesResource(QuerySequencePulsesService querySequencePulsesService) {
        this.querySequencePulsesService = querySequencePulsesService;
    }

    @GetMapping("time/{startTimestamp}/{endTimestamp}")
    @ResponseBody
    public ResponseEntity skypList(@PathVariable String startTimestamp, @PathVariable String endTimestamp){
        try {

            ZonedDateTime startTime = DateUtil.longToLocalDateTime(startTimestamp);
            ZonedDateTime endTime = DateUtil.longToLocalDateTime(endTimestamp);

            List<PulseDto> sequence = querySequencePulsesService.skiplist(endTime, startTime);

            SkiplistDto skiplist = new SkiplistDto(sequence);

            if (sequence==null){
                return ResourceResponseUtil.pulseNotAvailable();
            }
            return new ResponseEntity(skiplist, HttpStatus.OK);

        } catch (DateTimeParseException e){
            return ResourceResponseUtil.invalidCall();
        }
        catch (BadRequestException e){
            return ResourceResponseUtil.notImplemented();
        } catch (Exception e){
            e.printStackTrace();
            return ResourceResponseUtil.internalError();
        }
    }

    @GetMapping(value = {"/",""})
    @ResponseBody
    public ResponseEntity skypListParams(@RequestParam String anchorTime, @RequestParam String targetTime){

        return skypList(anchorTime,targetTime);

    }

}
