package com.example.beacon.vdf.sources;


import com.example.beacon.vdf.repository.AnuQRNGRemoteDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;

@Component
public class SeedAnuQuantumRNG implements SeedInterface {

    private final RestTemplate restTemplate;

    private static final String DESCRIPTION = "ANU Quantum's random bytes";

    private static boolean sended = false;
    private Environment env;

    private final Logger log = LoggerFactory.getLogger(SeedAnuQuantumRNG.class);

    @Autowired
    public SeedAnuQuantumRNG(RestTemplate restTemplate, Environment env){

        this.restTemplate = restTemplate;
        this.env = env;
    }

    @Override
    public SeedSourceDto getSeed() {

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("x-api-key",env.getProperty("x-api-key"));

        HttpEntity<String> entity = new HttpEntity<>("", headers);

        Instant now= Instant.now();
        int hour = now.atZone(ZoneOffset.UTC).getHour();
        int minute = now.atZone(ZoneOffset.UTC).getMinute();

        boolean threeTimesADay = (minute == 59 && (hour == 7 || hour == 15 || hour == 23)) || (minute <= 1) && (hour == 8 || hour == 14 || hour == 00);

        log.debug("getting anu seed at {}?: {}",now, threeTimesADay);

        if(threeTimesADay){

            log.debug("getting anu seed at {}",now);

            try{
                ResponseEntity<AnuQRNGRemoteDto>  response =restTemplate.exchange("https://api.quantumnumbers.anu.edu.au?length=5&type=hex16&size=8", HttpMethod.GET, entity, AnuQRNGRemoteDto.class);
                sended = true;
                AnuQRNGRemoteDto remoteDto = response.getBody();
                StringBuilder builder = new StringBuilder();
                remoteDto.getData().forEach(str-> builder.append(str));

                return new SeedSourceDto(now.toString(), "https://quantumnumbers.anu.edu.au/",
                        builder.toString() , DESCRIPTION, SeedAnuQuantumRNG.class);
            }catch (Exception e){
                log.warn(e.getMessage());
                log.debug(e.getStackTrace().toString());
            }
        }
        return null;
    }
}