package com.example.beacon.vdf.sources;


import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.example.beacon.vdf.repository.AnuQRNGRemoteDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;

import static com.cronutils.model.CronType.QUARTZ;

@Component
public class SeedAnuQuantumRNG implements SeedInterface {

    private final RestTemplate restTemplate;

    private static final String DESCRIPTION = "ANU Quantum's random bytes";

    private final Logger log = LoggerFactory.getLogger(SeedAnuQuantumRNG.class);

    final private String xapiKey;
    private final ExecutionTime executionTime;

    @Autowired
    public SeedAnuQuantumRNG(RestTemplate restTemplate, Environment env){

        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(QUARTZ);
        CronParser parser = new CronParser(cronDefinition);
        String expression = env.getProperty("anu.quantum.cron");

        Cron parsedQuartzCronExpression = parser.parse(expression);

        parsedQuartzCronExpression.validate();

        this.executionTime = ExecutionTime.forCron(parsedQuartzCronExpression);

        this.restTemplate = restTemplate;
        this.xapiKey = env.getProperty("x-api-key");
    }

    @Override
    public SeedSourceDto getSeed() {

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("x-api-key",this.xapiKey);

        HttpEntity<String> entity = new HttpEntity<>("", headers);

        ZonedDateTime now= ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC"));

        if(executionTime.isMatch(now)){

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