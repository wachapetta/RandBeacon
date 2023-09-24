package com.example.beacon.interfac.api.dto;

import com.example.beacon.interfac.domain.pulse.ListValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkiplistDto {
    private  long totalCount =0l;
    private boolean incomplete = false;
    private Map<String,String> links = new HashMap<String,String>();
//    private List<PulseDto> skiplist;
    private List<PulseSummaryDto> data;

    public SkiplistDto(List<PulseDto> sequence) {

        this.data = new ArrayList<PulseSummaryDto> ();
        this.totalCount = sequence.size();
        PulseSummaryDto d;

        for(PulseDto p : sequence){
            d = new PulseSummaryDto();
            d.setUri(p.getUri());
            d.setOutputVale(p.getOutputValue());
            data.add(d);
        }
        //data.sort((o1, o2) -> o1.getUri().compareTo(o2.getUri()));
    }
}
