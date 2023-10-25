package br.inmetro.gov.beacon.frontend.interfac.api;

import br.inmetro.gov.beacon.frontend.interfac.api.dto.PagedResponseDto;
import br.inmetro.gov.beacon.frontend.interfac.api.dto.PulseDto;
import br.inmetro.gov.beacon.frontend.interfac.api.dto.SkiplistDto;
import br.inmetro.gov.beacon.frontend.interfac.domain.service.BadRequestException;
import br.inmetro.gov.beacon.frontend.interfac.domain.service.QuerySequencePulsesService;
import br.inmetro.gov.beacon.frontend.vdf.infra.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;
import java.util.List;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Controller
@RequestMapping(value ={"/beacon/2.0/skiplist","/beacon/2.1/skiplist"}, produces= MediaType.APPLICATION_JSON_VALUE)
public class SequenceOfPulsesResource {

    private final QuerySequencePulsesService querySequencePulsesService;

    @Autowired
    public SequenceOfPulsesResource(QuerySequencePulsesService querySequencePulsesService) {
        this.querySequencePulsesService = querySequencePulsesService;
    }

    @GetMapping("time/{startTimestamp}/{endTimestamp}")
    @ResponseBody
    public ResponseEntity skypList(@PathVariable String startTimestamp, @PathVariable String endTimestamp, @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "0") int limit, HttpServletRequest request){
        try {


            if(offset<0 )
                return ResourceResponseUtil.createErrorResponse(HttpStatus.BAD_REQUEST,"Offset parameter must be a number >= 0");

            if(limit==0) limit = 25;

            if(limit >50 || limit <1)
                return ResourceResponseUtil.createErrorResponse(HttpStatus.BAD_REQUEST,"Limit parameter must be a number between 1 and 50.");

            ZonedDateTime startTime = DateUtil.longToLocalDateTime(startTimestamp);
            ZonedDateTime endTime = DateUtil.longToLocalDateTime(endTimestamp);

            if(endTime.isBefore(startTime))
                return ResourceResponseUtil.createErrorResponse(HttpStatus.BAD_REQUEST,"Anchor timestamp is before target timestamp.");


            if(request !=null && request.getRequestURI().contains("2.0")){
                List<PulseDto> sequence = querySequencePulsesService.sequence(startTime, endTime);
                SkiplistDto skiplist = new SkiplistDto(sequence);
                return new ResponseEntity(skiplist, HttpStatus.OK);
            }

            PagedResponseDto skipList = querySequencePulsesService.skiplist(endTime, startTime,offset,limit);

            if (skipList==null){
                return ResourceResponseUtil.pulseNotAvailable();
            }
            return new ResponseEntity(skipList, HttpStatus.OK);

        }
        catch (RuntimeException e){
            e.printStackTrace();
            return ResourceResponseUtil.createErrorResponse(HttpStatus.BAD_REQUEST,e.getMessage());
        }
        catch (Exception e){
            e.printStackTrace();
            return ResourceResponseUtil.internalError();
        }
    }

    @GetMapping(value = {"/",""})
    @ResponseBody
    public ResponseEntity skipListParams(@RequestParam(required = false) String anchorTime, @RequestParam(required = false) String targetTime, @RequestParam(required = false) String anchorId, @RequestParam(required = false) String targetId, @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "0") int limit, @RequestParam(defaultValue = "0") long chainId){

        if(chainId<1)
            return ResourceResponseUtil.createErrorResponse(HttpStatus.BAD_REQUEST,"Insufficient parameters! chainId must be set and cannot be <=0.");

        if(anchorTime==null && anchorId==null && targetTime== null && targetId == null)
            return ResourceResponseUtil.createErrorResponse(HttpStatus.BAD_REQUEST,"Insufficient parameters! You must set anchorId and targetId, or anchorTime and targetTime.");

        if(offset<0 )
            return ResourceResponseUtil.createErrorResponse(HttpStatus.BAD_REQUEST,"Offset parameter must be a number >= 0");

        if(limit==0) limit = 25;

        if(limit >50)
            return ResourceResponseUtil.createErrorResponse(HttpStatus.BAD_REQUEST,"Limit parameter must be a number between 1 and 50.");

        try {
            if (anchorTime != null && targetTime != null) {
                ZonedDateTime anchorT = DateUtil.longToLocalDateTime(anchorTime);
                ZonedDateTime targetT = DateUtil.longToLocalDateTime(targetTime);

                if(anchorT.isBefore(targetT))
                    return ResourceResponseUtil.createErrorResponse(HttpStatus.BAD_REQUEST,"Anchor timestamp is before target timestamp.");

                return skiplist(anchorT,targetT, offset, limit, chainId);

            }

            if (anchorId != null && targetId != null) {

                long anchorIdx = Long.parseLong(anchorId);
                long targetIdx = Long.parseLong(targetId);

                return skiplist(anchorIdx, targetIdx,offset, limit, chainId);
            }
        }catch (BadRequestException b){
            return ResourceResponseUtil.createErrorResponse(HttpStatus.BAD_REQUEST,b.getMessage());
        }

        return skypList(anchorTime,targetTime,offset, limit,null);

    }

    private ResponseEntity skiplist(ZonedDateTime anchorT,ZonedDateTime targetT, int offset, int limit, long chain) {
        PagedResponseDto skipList = querySequencePulsesService.skiplist(anchorT,targetT, offset, limit, chain);

        return new ResponseEntity(skipList, HttpStatus.OK);
    }

    private ResponseEntity skiplist(long anchorId,long targetId,int offset, int limit, long chain) {
        PagedResponseDto skipList = querySequencePulsesService.skiplist(anchorId,targetId, offset, limit, chain);

        return new ResponseEntity(skipList, HttpStatus.OK);
    }

}
