package com.example.beacon.vdf.sources;


import com.example.beacon.vdf.repository.AnuQRNGRemoteDto;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;

@Component
public class SeedAnuQuantumRNG implements SeedInterface {

    private final RestTemplate restTemplate;

    private static final String DESCRIPTION = "ANU Quantum's random bytes";

    private final Logger log = LoggerFactory.getLogger(SeedAnuQuantumRNG.class);

    private CronExpression expression;

    final private String xapiKey;

    @Autowired
    public SeedAnuQuantumRNG(RestTemplate restTemplate, Environment env){

        String tmp = env.getProperty("anu.quantum.cron");
        this.restTemplate = restTemplate;
        this.xapiKey = env.getProperty("x-api-key");

        if(tmp!=null && !tmp.equals("") && !tmp.equals("false")){
            try {
                this.expression = new CronExpression(tmp);
            } catch (ParseException e) {
            }
        }
    }

    @Override
    public SeedSourceDto getSeed() {

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("x-api-key",this.xapiKey);

        HttpEntity<String> entity = new HttpEntity<>("", headers);

        Instant now= Instant.now();
        int hour = now.atZone(ZoneOffset.UTC).getHour();
        int minute = now.atZone(ZoneOffset.UTC).getMinute();

        if(expression!=null && xapiKey!=null && expression.isSatisfiedBy(Date.from(now))){

            log.info("getting anu seed at {}",now);

            try{
                ResponseEntity<AnuQRNGRemoteDto>  response =restTemplate.exchange("https://api.quantumnumbers.anu.edu.au?length=5&type=hex16&size=8", HttpMethod.GET, entity, AnuQRNGRemoteDto.class);

                AnuQRNGRemoteDto remoteDto = response.getBody();

                if(remoteDto == null) return null;

                return new SeedSourceDto(now.toString(), "https://quantumnumbers.anu.edu.au/",
                        remoteDto.getRandom() , DESCRIPTION, SeedAnuQuantumRNG.class);
            }catch (Exception e){
                log.warn("Anu quantum service not available");
                log.error(e.getMessage());
                e.printStackTrace();
            }
        }
        return new SeedSourceDto(now.toString(), "https://quantumnumbers.anu.edu.au/",
                "", DESCRIPTION, SeedAnuQuantumRNG.class);
    }
}