package com.bricklink.api.html.support;

import feign.Response;

import java.lang.reflect.Type;

public interface ResponseAdapter<T> {
    T extract(Response response, Type type);
    Type getType();
}
