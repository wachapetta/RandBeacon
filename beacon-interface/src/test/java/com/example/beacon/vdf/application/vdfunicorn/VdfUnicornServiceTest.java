package com.example.beacon.vdf.application.vdfunicorn;

import com.example.beacon.vdf.scheduling.CombinationResultDto;
import com.example.beacon.vdf.sources.SeedCombinationResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;

@RunWith(SpringRunner.class)
@SpringBootTest
public class VdfUnicornServiceTest {

    @Autowired
    VdfUnicornService vdfUnicornService;

    @Autowired
    SeedCombinationResult seedCombinationResult;

    @Test
    public void test() throws Exception {
        vdfUnicornService.startTimeSlot();
        SeedPostDto seedPostDto = new SeedPostDto("a", "", "");
        vdfUnicornService.addSeed(seedPostDto);

        SeedPostDto seedPostDto2 = new SeedPostDto("b", "", "");
        vdfUnicornService.addSeed(seedPostDto2);

        seedCombinationResult.setCombinationResultDto(new CombinationResultDto(LocalDateTime.now().toString(),"c",""));
        vdfUnicornService.proceed();
    }

}