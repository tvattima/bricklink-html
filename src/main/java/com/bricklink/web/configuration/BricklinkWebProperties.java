package com.bricklink.web.configuration;

import com.bricklink.web.BricklinkWebException;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "bricklink.web")
public class BricklinkWebProperties {
    private Path clientConfigDir;
    private Path clientConfigFile;
    private Bricklink bricklink;

    public void setClientConfigDir(Path clientConfigDir) {
        this.clientConfigDir = clientConfigDir;
        loadPropertiesFromJson();
    }

    public void setClientConfigFile(Path clientConfigFile) {
        this.clientConfigFile = clientConfigFile;
        loadPropertiesFromJson();
    }

    public void writeJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
        mapper.writeValue(System.out, bricklink);
    }

    private void loadPropertiesFromJson() {
        Optional<Path> optionalDir = Optional.ofNullable(getClientConfigDir());
        Optional<Path> optionalFile = Optional.ofNullable(getClientConfigFile());
        if ((optionalDir.isPresent()) && (optionalFile.isPresent())) {
            Path jsonConfigFile = Paths.get(clientConfigDir.toString(), clientConfigFile.toString());
            if (Files.exists(jsonConfigFile)) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
                mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
                try {
                    bricklink = mapper.readValue(jsonConfigFile.toFile(), Bricklink.class);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            } else {
                throw new IllegalStateException("[" + jsonConfigFile.toAbsolutePath() + "] does not exist");
            }
        }
    }

    public URL getURL(String name) {
        return Optional.ofNullable(bricklink.getUrls().get(name)).orElseThrow(() -> new BricklinkWebException("Unknown page requested ["+name+"]"));
    }

    public Bricklink getBricklink() {
        return Optional.ofNullable(bricklink)
                       .orElseThrow(() -> new IllegalStateException("Bricklink properties have not been loaded"));
    }

    @Data
    @JsonRootName(value = "bricklink")
    public static class Bricklink {
        private Credential credential;
        private Map<String, URL> urls;
    }

    @Data
    public static class Credential {
        private String username;
        private String password;
    }
}

