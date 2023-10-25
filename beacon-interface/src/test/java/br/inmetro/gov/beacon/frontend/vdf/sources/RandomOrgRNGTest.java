package br.inmetro.gov.beacon.frontend.vdf.sources;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;


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
