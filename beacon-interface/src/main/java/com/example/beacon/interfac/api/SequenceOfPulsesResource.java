package com.example.beacon.interfac.api;

import com.example.beacon.interfac.api.dto.PulseDto;
import com.example.beacon.interfac.api.dto.PagedResponseDto;
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
    public ResponseEntity skypList(@PathVariable String startTimestamp, @PathVariable String endTimestamp,@RequestParam(defaultValue = "0") int offset,@RequestParam(defaultValue = "0") int limit){
        try {

            if(offset<0 )
                return ResourceResponseUtil.createErrorResponse(HttpStatus.BAD_REQUEST,"Offset parameter must be a number >= 0");

            if(limit==0) limit = 25;

            if(limit >50)
                return ResourceResponseUtil.createErrorResponse(HttpStatus.BAD_REQUEST,"Limit parameter must be a number between 1 and 50.");

            ZonedDateTime startTime = DateUtil.longToLocalDateTime(startTimestamp);
            ZonedDateTime endTime = DateUtil.longToLocalDateTime(endTimestamp);


            PagedResponseDto skipList = querySequencePulsesService.skiplist(endTime, startTime,offset,limit);

            if (skipList==null){
                return ResourceResponseUtil.pulseNotAvailable();
            }
            return new ResponseEntity(skipList, HttpStatus.OK);

        } catch (DateTimeParseException e){
            return ResourceResponseUtil.invalidCall();
        }
        catch (RuntimeException e){
            return ResourceResponseUtil.invalidCall();
        }
        catch (Exception e){
            e.printStackTrace();
            return ResourceResponseUtil.internalError();
        }
    }

    @GetMapping(value = {"/",""})
    @ResponseBody
    public ResponseEntity skypListParams(@RequestParam String anchorTime, @RequestParam String targetTime,@RequestParam(defaultValue = "0") int offset,@RequestParam(defaultValue = "0") int limit){

        return skypList(targetTime,anchorTime,offset, limit);

    }

}
