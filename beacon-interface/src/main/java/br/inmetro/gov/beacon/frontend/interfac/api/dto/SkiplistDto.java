package br.inmetro.gov.beacon.frontend.interfac.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkiplistDto {
    private List<PulseDto> skiplist;
}
