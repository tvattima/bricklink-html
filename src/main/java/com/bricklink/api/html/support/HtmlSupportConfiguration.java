package com.bricklink.api.html.support;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class HtmlSupportConfiguration {

    @Bean
    public ResponseAdapterRegistry responseAdapterRegistry(List<ResponseAdapter<?>> responseAdapters) {
        ResponseAdapterRegistry responseAdapterRegistry = new ResponseAdapterRegistry();
        for (ResponseAdapter responseAdapter : responseAdapters) {
            responseAdapterRegistry.registerResponseAdapter(responseAdapter.getType(), responseAdapter);
        }
        return responseAdapterRegistry;
    }
}
