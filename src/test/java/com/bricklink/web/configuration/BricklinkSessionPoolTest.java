package com.bricklink.web.configuration;

import com.bricklink.web.support.BricklinkSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.ObjectPool;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {BricklinkSessionPoolTest.MyTestConfiguration.class, BricklinkWebConfiguration.class})
@Slf4j
public class BricklinkSessionPoolTest {
    @Autowired
    ObjectPool<BricklinkSession> pool;

    @Test
    public void pool_isNotEmpty() throws Exception {
        List<BricklinkSession> sessions = new ArrayList<>();
        try {
            for (int i = 0; i < 6; i++) {
                sessions.add(pool.borrowObject());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            sessions.forEach(bs -> {
                try {
                    pool.returnObject(bs);
                } catch (Exception e1) {
                    log.error(e1.getMessage(), e1);
                }
            });
        }
    }

    @Configuration
    static class MyTestConfiguration {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        public BricklinkWebProperties bricklinkWebProperties() {
            BricklinkWebProperties properties = new BricklinkWebProperties();
            properties.setClientConfigDir(Paths.get("C:\\Users\\tvatt\\.credentials\\bricklink"));
            properties.setClientConfigFile(Paths.get("bricklink-web.json"));
            properties.setPool(new BricklinkWebProperties.Pool());
            BricklinkWebProperties.Pool pool = properties.getPool();
            pool.setSize(5);
            return properties;
        }

    }
}
