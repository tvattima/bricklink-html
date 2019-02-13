package com.bricklink.api.html.htmlunit;

import com.bricklink.api.html.support.ResponseAdapter;
import com.bricklink.api.html.support.ResponseAdapterRegistry;
import feign.FeignException;
import feign.Response;
import feign.codec.Decoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class HtmlUnitDecoder implements Decoder {

    private final ResponseAdapterRegistry responseAdapterRegistry;

    @Override
    public Object decode(Response response, Type type) throws FeignException {
        Optional<ResponseAdapter<?>> responseAdapter = responseAdapterRegistry.getResponseAdapter(type);
        if (responseAdapter.isPresent()) {
            return responseAdapter.get().extract(response, type);
        } else {
            return new Object();
        }
    }
}
