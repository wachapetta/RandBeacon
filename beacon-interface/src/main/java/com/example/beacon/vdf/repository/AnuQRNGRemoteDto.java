package com.example.beacon.vdf.repository;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("pulse")
//@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT ,use = JsonTypeInfo.Id.NAME)
public class AnuQRNGRemoteDto {
    private List<String> data;

    public String getRandom(){
        StringBuilder builder = new StringBuilder();

        getData().forEach(str-> builder.append(str));

        return builder.toString();
    }

}
