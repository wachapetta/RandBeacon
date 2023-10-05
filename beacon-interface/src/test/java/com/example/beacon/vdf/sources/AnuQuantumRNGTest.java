package com.example.beacon.vdf.sources;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZoneId;

import static com.cronutils.model.CronType.QUARTZ;





//@RunWith(PowerMockRunner.class)
//@PrepareForTest( {SeedAnuQuantumRNG.class,RestTemplate.class, Environment.class,Instant.class})
@SpringBootTest
@TestPropertySource(properties = {"anu.quantum.cron = * 54-56 7,15,23 * * ? *"})

public class AnuQuantumRNGTest {



    public Environment env;

    @Test
    public void testAnuQuantumExpression() {

        /*String instantExp = "2024-12-22T07:54:00Z";
        Instant ti1 = Instant.parse(instantExp);
        mockStatic(Instant.class);
        when(Instant.now()).thenReturn(ti1);

        RestTemplate restTemplate = new RestTemplate();

        SeedAnuQuantumRNG obj = new SeedAnuQuantumRNG(restTemplate,env);

        SeedSourceDto seed = obj.getSeed();

        System.out.println(seed.getSeed());

*/

        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(QUARTZ);

        CronParser parser = new CronParser(cronDefinition);
        Cron parsedQuartzCronExpression = parser.parse("* 54-56 7,15,23 * * ? *");

        parsedQuartzCronExpression.validate();

        ExecutionTime executionTime = ExecutionTime.forCron(parsedQuartzCronExpression);

        String instantExpected = "2024-12-22T07:54:00Z";
        Instant time = Instant.parse(instantExpected);

        Assert.assertTrue(executionTime.isMatch(time.atZone(ZoneId.of("UTC"))));


        instantExpected = "2024-12-22T07:56:59Z";
        time = Instant.parse(instantExpected);

        Assert.assertTrue(executionTime.isMatch(time.atZone(ZoneId.of("UTC"))));


        instantExpected = "2024-12-22T07:55:00Z";
        time = Instant.parse(instantExpected);

        Assert.assertTrue(executionTime.isMatch(time.atZone(ZoneId.of("UTC"))));


        instantExpected = "2014-12-22T07:53:59Z";
        time = Instant.parse(instantExpected);

        Assert.assertFalse(executionTime.isMatch(time.atZone(ZoneId.of("UTC"))));

        instantExpected = "2014-12-22T23:57:00Z";
        time = Instant.parse(instantExpected);

        Assert.assertFalse(executionTime.isMatch(time.atZone(ZoneId.of("UTC"))));


    }

}
