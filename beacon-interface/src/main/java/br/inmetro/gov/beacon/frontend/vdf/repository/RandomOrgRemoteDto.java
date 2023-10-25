package br.inmetro.gov.beacon.frontend.vdf.repository;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("result")
//@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT ,use = JsonTypeInfo.Id.NAME)
public class RandomOrgRemoteDto {

    Map<String,Object> result;

    public String getRandomValue(){
        Map<String,Object> random = (Map<String, Object>) getResult().get("random");
        List<String> data = (List<String>) random.get("data");
        StringBuilder builder = new StringBuilder();

        data.forEach(s -> builder.append(s));

        return builder.toString();
    }

}
