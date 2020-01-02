package com.bricklink.web.support;

import com.bricklink.web.configuration.BricklinkWebConfiguration;
import com.bricklink.web.configuration.BricklinkWebProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vattima.lego.imaging.service.ImageScalingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.HttpClientConnectionManager;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
//@RunWith(SpringRunner.class)
//@ContextConfiguration(classes = {BricklinkWebServiceTest.MyTestConfiguration.class, BricklinkWebConfiguration.class})
public class BricklinkWebServiceTest {
    @Autowired
    private ConnectionKeepAliveStrategy connectionKeepAliveStrategy;

    @Autowired
    private HttpClientConnectionManager httpClientConnectionManager;

    @Autowired
    private BricklinkWebProperties properties;

    @Autowired
    private ObjectMapper mapper;

    @Test
    @Ignore
    public void uploadInventoryImage_authenticatesUploadsAndDoesLogout() throws Exception {
        BricklinkSession bricklinkSession = null;
        StopWatch timer = new StopWatch();
        timer.start();
        try {
            ObjectMapper mapper = new ObjectMapper();

            BricklinkWebService bricklinkWebService = new BricklinkWebService(httpClientConnectionManager, properties, mapper, connectionKeepAliveStrategy);
            bricklinkWebService.authenticate();
//            bricklinkWebService.uploadInventoryImage(171330559L, Paths.get("C:\\Users\\tvatt\\Desktop\\lego-collection-photos\\6658-1-f81eb8d9ddc469b48dff9c45064be5eb\\bl-inventory-photo.jpg"));
            bricklinkSession = bricklinkWebService.logout();
        } finally {
            timer.stop();
        }
        log.info("uploaded photo [{}} to inventory [{}] in [{}] ms", "abc", 1234567L, timer.getTotalTimeMillis());

        assertThat(bricklinkSession).isNotNull();
    }

    @Test
    @Ignore
    public void uploadInventory_multiThreaded() {
        ImageScalingService imageScalingService = new ImageScalingService();
        BricklinkWebService bricklinkWebService = new BricklinkWebService(httpClientConnectionManager, properties, mapper, connectionKeepAliveStrategy);
        BricklinkSession bricklinkSession = bricklinkWebService.authenticate();
        Map<Long, Path> inventoryPhotos = new HashMap<>();
//        inventoryPhotos.put(171947252L, Paths.get("C:\\Users\\tvatt\\Desktop\\lego-collection-photos\\6641-1-a89ad8dcf82e868a5092d4a2376f8981\\DSC_0593.JPG"));
//        inventoryPhotos.put(171947247L, Paths.get("C:\\Users\\tvatt\\Desktop\\lego-collection-photos\\646-1-c24f8e2ac2e40a63ba3b253316c32fb9\\DSC_0582.JPG"));
//        inventoryPhotos.put(171947253L, Paths.get("C:\\Users\\tvatt\\Desktop\\lego-collection-photos\\6658-1-f81eb8d9ddc469b48dff9c45064be5eb\\DSC_0516.JPG"));
//        inventoryPhotos.put(171947228L, Paths.get("C:\\Users\\tvatt\\Desktop\\lego-collection-photos\\6627-1-9b665132a4001f281d6cce8b0033ae7b\\DSC_0628.JPG"));
//        inventoryPhotos.put(171947231L, Paths.get("C:\\Users\\tvatt\\Desktop\\lego-collection-photos\\6624-1-88ad09baa1f934961ada875b0c279273\\DSC_0657.JPG"));
//        inventoryPhotos.put(171947251L, Paths.get("C:\\Users\\tvatt\\Desktop\\lego-collection-photos\\6521-1-85179faa62d34993132558d1dcea6bc2\\DSC_0619.JPG"));
//        inventoryPhotos.put(171947248L, Paths.get("C:\\Users\\tvatt\\Desktop\\lego-collection-photos\\6633-1-3104970a366f472405a5ee30756b7e70\\DSC_0643.JPG"));
//        inventoryPhotos.put(171947250L, Paths.get("C:\\Users\\tvatt\\Desktop\\lego-collection-photos\\6657-1-bb994be7893e07178ab2c9cec6e0b95b\\DSC_0521.JPG"));
//        inventoryPhotos.put(171947256L, Paths.get("C:\\Users\\tvatt\\Desktop\\lego-collection-photos\\6643-1-f941eca96f5e873077fe3186631240fd\\DSC_0554.JPG"));
//        inventoryPhotos.put(171947259L, Paths.get("C:\\Users\\tvatt\\Desktop\\lego-collection-photos\\6653-1-739f32c367ed2aead1b4adcddb5a50a1\\DSC_0532.JPG"));
        StopWatch timer = new StopWatch();
        timer.start();
        try {
            inventoryPhotos.keySet().parallelStream().forEach(k -> {
                Path p = inventoryPhotos.get(k);
                Path resizedImagePath = imageScalingService.scale(p);
                bricklinkWebService.uploadInventoryImage(k, resizedImagePath);
            });
        } finally {
            bricklinkWebService.logout();
            timer.stop();
        }
    }

    @Configuration
    static class MyTestConfiguration {
        @Bean
        public BricklinkWebProperties bricklinkWebProperties() {
            BricklinkWebProperties properties = new BricklinkWebProperties();
            properties.setClientConfigDir(Paths.get("C:\\Users\\tvatt\\.credentials\\bricklink"));
            properties.setClientConfigFile(Paths.get("bricklink-web.json"));
            properties.setPool(new BricklinkWebProperties.Pool());
            properties.getPool()
                      .setDefaultMaxPerRoute(20);
            properties.getPool()
                      .setMaxPerRoute(50);
            properties.getPool()
                      .setMaxTotal(200);
            return properties;
        }

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}