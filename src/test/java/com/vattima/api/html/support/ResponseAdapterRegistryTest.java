package com.bricklink.api.html.support;

import feign.Response;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ResponseAdapterRegistryTest {

    @Test
    public void test_RegisterMultiple() {
        ResponseAdapter<A> adapterA = new AResponseAdapter();
        ResponseAdapter<B> adapterB = new BResponseAdapter();
        ResponseAdapter<C> adapterC = new CResponseAdapter();

        ResponseAdapterRegistry registry = new ResponseAdapterRegistry();
        registry.registerResponseAdapter(A.class, adapterA);
        registry.registerResponseAdapter(B.class, adapterB);
        registry.registerResponseAdapter(C.class, adapterC);

        Optional optional = registry.getResponseAdapter(A.class);
        assertThat(optional.isPresent()).isTrue();
        assertThat(optional.get()).isEqualTo(adapterA);

        optional = registry.getResponseAdapter(B.class);
        assertThat(optional.isPresent()).isTrue();
        assertThat(optional.get()).isEqualTo(adapterB);

        optional = registry.getResponseAdapter(C.class);
        assertThat(optional.isPresent()).isTrue();
        assertThat(optional.get()).isEqualTo(adapterC);
    }

    private static class A {

    }

    private static class B {

    }

    private static class C {

    }

    private static class AResponseAdapter implements ResponseAdapter<A> {
        @Override
        public A extract(Response response, Type type) {
            return new A();
        }

        @Override
        public Type getType() {
            return A.class;
        }
    }

    private static class BResponseAdapter implements ResponseAdapter<B> {
        @Override
        public B extract(Response response, Type type) {
            return new B();
        }

        @Override
        public Type getType() {
            return B.class;
        }
    }

    private static class CResponseAdapter implements ResponseAdapter<C> {
        @Override
        public C extract(Response response, Type type) {
            return new C();
        }

        @Override
        public Type getType() {
            return C.class;
        }
    }
}