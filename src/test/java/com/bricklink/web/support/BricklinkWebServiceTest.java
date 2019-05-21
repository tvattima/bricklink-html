package com.bricklink.web.support;

import com.bricklink.web.configuration.BricklinkWebProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.util.StopWatch;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class BricklinkWebServiceTest {
    @Test
    public void canAuthenticate() throws Exception {
        BricklinkSession bricklinkSession = null;
        StopWatch timer = new StopWatch();
        timer.start();
        try {
            BricklinkWebProperties properties = new BricklinkWebProperties();
            properties.setClientConfigDir(Paths.get("C:\\Users\\tvatt\\.credentials\\bricklink"));
            properties.setClientConfigFile(Paths.get("bricklink-web.json"));
            ObjectMapper mapper = new ObjectMapper();

            BricklinkWebService bricklinkWebService = new BricklinkWebService(properties, mapper);
            bricklinkSession = bricklinkWebService.authenticate();
            bricklinkWebService.uploadInventoryImage(bricklinkSession, 171330559L, Paths.get("C:\\Users\\tvatt\\Desktop\\lego-collection-photos\\6658-1-f81eb8d9ddc469b48dff9c45064be5eb\\bl-inventory-photo.jpg"));
            bricklinkSession = bricklinkWebService.logout(bricklinkSession);
        } finally {
            timer.stop();
        }
        log.info("uploaded photo [{}} to inventory [{}] in [{}] ms", "abc", 1234567L, timer.getTotalTimeMillis());

        assertThat(bricklinkSession).isNotNull();
    }
}