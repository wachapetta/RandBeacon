package br.inmetro.gov.beacon.frontend.vdf.sources;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;

import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static com.cronutils.model.CronType.QUARTZ;

@RunWith(SpringRunner.class)
@TestPropertySource(properties = {"anu.quantum.cron = * 54-56 7,15,23 * * ? *"})
public class AnuQuantumRNGTest {


    public Environment env = new AbstractEnvironment() {

        public String getProperty(String key) {

            Instant now = Instant.now();
            String expression = "* {0} {1} * * ? *";
            ZonedDateTime zonedNow = now.atZone(ZoneId.of("UTC"));

            String minutes = String.valueOf(zonedNow.getMinute())+","+String.valueOf(zonedNow.getMinute()+1);
            if(key == "anu.quantum.cron") {
                expression = expression.replace("{0}",minutes);
                expression = expression.replace("{1}",String.valueOf(zonedNow.getHour()));
                return expression;
            }

            return System.getenv(key);
        }

    };

    @Test
    public void testAnuQuantum() {
        /*String instantExp = "2024-12-22T07:54:00Z";
        ZonedDateTime ti1 = ZonedDateTime.parse(instantExp);
        mockStatic(DateUtil.class);
        when(DateUtil.now()).thenReturn(ti1);*/

        RestTemplate restTemplate = new RestTemplate();

        SeedAnuQuantumRNG obj = new SeedAnuQuantumRNG(restTemplate, env);
        SeedSourceDto result = obj.getSeed();

        System.out.println(result.getSeed()+" "+result.getUri());

        CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(QUARTZ);
        CronParser parser = new CronParser(cronDefinition);
        String expression = "* 54-56 7,15,23 * * ? *";

        Cron parsedQuartzCronExpression = parser.parse(expression);

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
