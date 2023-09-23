package com.example.beacon.vdf.sources;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.web.client.RestTemplate;

public class AnuQuantumRNGTest {


    public Environment env = new Environment() {
        @Override
        public String[] getActiveProfiles() {
            return new String[0];
        }

        @Override
        public String[] getDefaultProfiles() {
            return new String[0];
        }

        @Override
        public boolean acceptsProfiles(String... profiles) {
            return false;
        }

        @Override
        public boolean acceptsProfiles(Profiles profiles) {
            return false;
        }

        @Override
        public boolean containsProperty(String key) {
            return false;
        }

        @Override
        public String getProperty(String key) {
            return "VS76kiBiXSaqjJHHy4B5o9z686jHKMDvYzsnq2k1";
        }

        @Override
        public String getProperty(String key, String defaultValue) {
            return null;
        }

        @Override
        public <T> T getProperty(String key, Class<T> targetType) {
            return null;
        }

        @Override
        public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
            return null;
        }

        @Override
        public String getRequiredProperty(String key) throws IllegalStateException {
            return null;
        }

        @Override
        public <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException {
            return null;
        }

        @Override
        public String resolvePlaceholders(String text) {
            return null;
        }

        @Override
        public String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
            return null;
        }
    };

    @Test
    public void testSeed(){

        RestTemplate restTemplate = new RestTemplate();

        SeedAnuQuantumRNG obj = new SeedAnuQuantumRNG(restTemplate, env);

        SeedSourceDto seed = obj.getSeed();

        System.out.println(seed.getUri());
        System.out.println(seed.getTimeStamp());
        System.out.println(seed.getDescription());
        System.out.println(seed.getSeed());

        Assert.assertTrue(true);


    }

}
