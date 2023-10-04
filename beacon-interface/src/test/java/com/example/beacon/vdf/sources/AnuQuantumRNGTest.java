package com.example.beacon.vdf.sources;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extensions;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.plugins.MockMaker;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

import static org.mockito.Mockito.mockStatic;


@RunWith(SpringRunner.class)
@TestPropertySource(properties = {"anu.quantum.cron = * 54,55,56 * * * ? *"})
public class AnuQuantumRNGTest {

    public Environment env = Mockito.mock(Environment.class);

    //public MockMaker maker = MockMaker.;

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRandomOrg(){

        RestTemplate restTemplate = new RestTemplate();

        SeedRandomOrgRNG obj = new SeedRandomOrgRNG(restTemplate, env);

        SeedSourceDto seed = obj.getSeed();

        System.out.println(seed.getSeed());

        Assert.assertTrue(seed!=null);

    }

    @Test
    public void testAnuQuantum() throws ParseException {

        RestTemplate restTemplate = new RestTemplate();

        SeedAnuQuantumRNG obj = new SeedAnuQuantumRNG(restTemplate,env);

        SeedSourceDto seed = obj.getSeed();

        System.out.println(seed.getSeed());



        CronExpression expression = new CronExpression("* 54,55,56 7,15,23 * * ? *");

        String instantExpected = "2014-12-22T07:56:56Z";
        Instant time = Instant.parse(instantExpected);
        Assert.assertTrue(expression.isSatisfiedBy(Date.from(time)));

        instantExpected = "2014-12-22T23:54:54Z";
        time = Instant.parse(instantExpected);
        Assert.assertTrue(expression.isSatisfiedBy(Date.from(time)));

        instantExpected = "2014-12-22T15:55:55Z";
        time = Instant.parse(instantExpected);
        Assert.assertTrue(expression.isSatisfiedBy(Date.from(time)));

        Assert.assertTrue(seed!=null);

    }

}
