package com.example.beacon.vdf.sources;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.mockito.invocation.InvocationOnMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.cronutils.model.CronType.QUARTZ;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;



//@PrepareForTest( {DateUtil.class})
@RunWith(SpringRunner.class)
@TestPropertySource(properties = {"anu.quantum.cron = * 54-56 7,15,23 * * ? *"})
public class AnuQuantumRNGTest {

    @Autowired
    public Environment env;

    @Test
    public void testAnuQuantum() throws NoSuchFieldException, IllegalAccessException {
        /*String instantExp = "2024-12-22T07:54:00Z";
        ZonedDateTime ti1 = ZonedDateTime.parse(instantExp);
        mockStatic(DateUtil.class);
        when(DateUtil.now()).thenReturn(ti1);*/

        Clock.systemDefaultZone();

        RestTemplate restTemplate = new RestTemplate();

        SeedAnuQuantumRNG obj = Mockito.mock(SeedAnuQuantumRNG.class);

        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(QUARTZ);
        CronParser parser = new CronParser(cronDefinition);
        String expression = "* 54,55,56 7,15,23 * * ? *";

        Cron parsedQuartzCronExpression = parser.parse(expression);

        parsedQuartzCronExpression.validate();

        ExecutionTime executionTime = ExecutionTime.forCron(parsedQuartzCronExpression);

        ReflectionTestUtils.setField(obj,"executionTime",executionTime);

        //when(obj.getNow()).thenReturn(ZonedDateTime.now());

        when(obj.getSeed()).then(InvocationOnMock::callRealMethod);


        SeedSourceDto seed = obj.getSeed();

        System.out.println(seed.getSeed());

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
