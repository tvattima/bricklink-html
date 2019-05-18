package com.bricklink.web.support;

import com.bricklink.web.configuration.BricklinkWebProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class BricklinkWebServiceTest {
    @Test
    public void canAuthenticate() throws Exception {
        BricklinkWebProperties properties = new BricklinkWebProperties();
        properties.setClientConfigDir(Paths.get("C:\\Users\\tvatt\\.credentials\\bricklink"));
        properties.setClientConfigFile(Paths.get("bricklink-web.json"));
        ObjectMapper mapper = new ObjectMapper();

        BricklinkWebService bricklinkWebService = new BricklinkWebService(properties, mapper);
        BricklinkSession bricklinkSession = bricklinkWebService.authenticate();
        bricklinkWebService.uploadInventoryImage(bricklinkSession, 171284300L, Paths.get("C:\\Users\\tvatt\\Desktop\\lego-collection-photos\\6658-1-f81eb8d9ddc469b48dff9c45064be5eb\\bl-inventory-photo.jpg"));
        bricklinkSession = bricklinkWebService.logout(bricklinkSession);

        assertThat(bricklinkSession).isNotNull();
    }
}