package com.example.beacon.vdf.sources;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;

import static com.cronutils.model.CronType.QUARTZ;


@RunWith(SpringRunner.class)
public class RandomOrgRNGTest {


    @Autowired
    public Environment env;

    @Test
    public void testRandomOrg(){

        RestTemplate restTemplate = new RestTemplate();

        SeedRandomOrgRNG obj = new SeedRandomOrgRNG(restTemplate, env);

        SeedSourceDto seed = obj.getSeed();

        System.out.println(seed.getSeed());

        Assert.assertTrue(seed!=null);

    }

}
