    package com.example.beacon.vdf.sources;


    import com.example.beacon.vdf.repository.RandomOrgRemoteDto;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.*;
    import org.springframework.stereotype.Component;
    import org.springframework.web.client.RestTemplate;

    import java.time.Instant;
    import java.util.Collections;

    @Component
    public class SeedRandomOrgRNG implements SeedInterface {

        private final RestTemplate restTemplate;

        private static final String DESCRIPTION = "Random.org's random bytes";

        private final Logger log = LoggerFactory.getLogger(SeedRandomOrgRNG.class);

        @Autowired
        public SeedRandomOrgRNG(RestTemplate restTemplate){

            this.restTemplate = restTemplate;
        }

        @Override
        public SeedSourceDto getSeed() {

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);

            StringBuilder body = new StringBuilder();

            body.append("{").
                append("\"jsonrpc\": \"2.0\",").
                    append(" \"method\": \"generateIntegers\",").
                    append("\"params\": {").
                    append("\"apiKey\": \"5838ac9d-9085-4d34-b251-4ea0305c2dd8\",").
                    append("\"n\": 64,").
                    append("\"min\": 0,").
                    append("\"max\": 255,").
                    append("\"replacement\": true,").
                    append("\"base\": 16,").
                    append("\"pregeneratedRandomization\": null").
                    append("},").
                    append("\"id\": 7428").
                    append("}");

            HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);

            try{
                ResponseEntity<RandomOrgRemoteDto>  response =restTemplate.exchange("https://api.random.org/json-rpc/4/invoke", HttpMethod.POST, entity, RandomOrgRemoteDto.class);

                RandomOrgRemoteDto remoteDto = response.getBody();

                if(remoteDto == null)
                    return null;

                return new SeedSourceDto(Instant.now().toString(), "https://api.random.org/json-rpc/4/invoke",
                        remoteDto.getRandomValue(), DESCRIPTION, SeedRandomOrgRNG.class);
            }catch (Exception e){
                    log.warn("Random.org service not available");
            }
            return null;
        }
    }