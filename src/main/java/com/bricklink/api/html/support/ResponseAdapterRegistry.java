package com.bricklink.api.html.support;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ResponseAdapterRegistry {
    private ConcurrentHashMap<Type, ResponseAdapter<?>> registry = new ConcurrentHashMap<>();

    public void registerResponseAdapter(Type type, ResponseAdapter<?> responseAdapter) {
        registry.put(type, responseAdapter);
    }

    public Optional<ResponseAdapter<?>> getResponseAdapter(Type type) {
        return Optional.ofNullable(registry.get(type));
    }
}
