package com.example.beacon.cache;

import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheEventLogger
        implements CacheEventListener<Object, Object> {

    private static final Logger logger = LoggerFactory.getLogger(CacheEventListener.class);

    @Override
    public void onEvent(
            CacheEvent<? extends Object, ? extends Object> cacheEvent) {
        logger.warn("cache:" + cacheEvent.getKey() +  cacheEvent.getOldValue() +  cacheEvent.getNewValue());
    }
}