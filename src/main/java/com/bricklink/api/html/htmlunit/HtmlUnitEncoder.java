package com.bricklink.api.html.htmlunit;

import feign.RequestTemplate;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;

@Slf4j
public class HtmlUnitEncoder implements Encoder {
    @Override
    public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
        log.info("Object [{}], bodyType [{}], template [{}]", object, bodyType, template);
    }
}
