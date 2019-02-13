package com.bricklink.api.html.support;

import feign.Request;
import feign.RequestTemplate;
import feign.Target;

public class BricklinkHtmlTarget<T> implements Target<T> {

    private final Class<T> type;
    private final String url;

    public BricklinkHtmlTarget(Class<T> type, String url) {
        this.type = type;
        this.url = url;
    }

    @Override
    public Class<T> type() {
        return type;
    }

    @Override
    public String name() {
        return "Bricklink HTML com.bricklink.api.ajax.model.v1";
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public Request apply(RequestTemplate requestTemplate) {
        requestTemplate.insert(0, url);
        return requestTemplate.request();
    }
}
