package br.inmetro.gov.beacon.frontend.cache;

import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class BeaconCacheCustomizer implements CacheManagerCustomizer<ConcurrentMapCacheManager> {
    @Override
    public void customize(ConcurrentMapCacheManager cacheManager) {

        cacheManager.setCacheNames(Arrays.asList("countSkipLists"));
        cacheManager.setCacheNames(Arrays.asList("countSkipListsByChainAndIndexes"));
    }
}
