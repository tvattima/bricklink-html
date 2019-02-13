package com.bricklink.api.html.configuration;

import com.bricklink.api.html.BricklinkHtmlClient;
import com.bricklink.api.html.htmlunit.HtmlUnitClient;
import com.bricklink.api.html.htmlunit.HtmlUnitDecoder;
import com.bricklink.api.html.htmlunit.HtmlUnitEncoder;
import com.bricklink.api.html.support.BricklinkHtmlTarget;
import com.bricklink.api.html.support.ResponseAdapterRegistry;
import feign.Feign;
import feign.slf4j.Slf4jLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BricklinkApiHtmlConfiguration {
    @Bean
    public BricklinkHtmlClient getBrickLinkHtmlClient(final ResponseAdapterRegistry responseAdapterRegistry) {
        return Feign
                .builder()
                .client(new HtmlUnitClient())
                .encoder(new HtmlUnitEncoder())
                .decoder(new HtmlUnitDecoder(responseAdapterRegistry))
                .logger(new Slf4jLogger(BricklinkHtmlClient.class))
                .logLevel(feign.Logger.Level.FULL)
                .target(new BricklinkHtmlTarget<>(BricklinkHtmlClient.class, "https://www.bricklink.com"));
    }
}
