package br.inmetro.gov.beacon.frontend.interfac.web;

import br.inmetro.gov.beacon.frontend.interfac.domain.service.ActiveChainService;
import br.inmetro.gov.beacon.frontend.interfac.domain.service.QuerySinglePulsesService;
import br.inmetro.gov.beacon.frontend.interfac.infra.AppUri;
import br.inmetro.gov.beacon.frontend.interfac.api.dto.PulseDto;
import br.inmetro.gov.beacon.frontend.vdf.infra.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.time.temporal.ChronoUnit;

@Controller
@RequestMapping("/")
public class HomeController {

    private final AppUri appUri;

    private final QuerySinglePulsesService querySinglePulsesService;
    private final long chain;

    @Autowired
    public HomeController(AppUri appUri, QuerySinglePulsesService querySinglePulsesService, ActiveChainService chainService) {
        this.appUri = appUri;
        this.querySinglePulsesService = querySinglePulsesService;
        this.chain = chainService.get().getChainIndex();
    }

    @GetMapping
    public ModelAndView home(){
        ModelAndView mv = new ModelAndView("home/index");
        mv.addObject("uri", appUri.getUri());

        PulseDto current = querySinglePulsesService.lastDto(chain);
        PulseDto first = querySinglePulsesService.firstDto(chain);
        PulseDto previous = querySinglePulsesService.findByChainAndPulseId(chain,current.getPulseIndex()-1);

        if (current!=null){
            mv.addObject("timestampPrevious", DateUtil.datetimeToMilli(previous.getTimeStamp()));
            mv.addObject("timestampCurrent",  DateUtil.datetimeToMilli(current.getTimeStamp().truncatedTo(ChronoUnit.MINUTES)));
            mv.addObject("pulseIndexPrevious", previous.getPulseIndex());
            mv.addObject("chain",chain);
            mv.addObject("currentId",current.getPulseIndex());
            mv.addObject("previousId", previous.getPulseIndex());
            mv.addObject("firstTimestamp",DateUtil.datetimeToMilli(first.getTimeStamp()));
            mv.addObject("firstId",first.getPulseIndex());
        }

        return mv;
    }

}
