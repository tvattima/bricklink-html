package com.bricklink.web.configuration;

import com.bricklink.web.support.BricklinkSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BricklinkWebConfiguration {
    @Bean
    public PooledObjectFactory<BricklinkSession> pooledObjectFactory(final BricklinkWebProperties bricklinkWebProperties, final ObjectMapper objectMapper) {
        return new BricklinkSessionPooledObjectFactory(bricklinkWebProperties, objectMapper);
    }

    @Bean
    public ObjectPool<BricklinkSession> bricklinkSessionPool(final BricklinkWebProperties bricklinkWebProperties, final PooledObjectFactory<BricklinkSession> pooledObjectFactory) {
        GenericObjectPoolConfig<BricklinkSession> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxIdle(1);
        poolConfig.setMinIdle(1);
        poolConfig.setMaxTotal(bricklinkWebProperties.getPool().getSize());
        return new GenericObjectPool<>(pooledObjectFactory);
    }
}
