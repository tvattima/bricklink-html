package com.bricklink.web.support;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BricklinkWebAuthenticatorTest {
    @Test
    public void canAuthenticate() throws Exception {
        BricklinkWebAuthenticator bricklinkWebAuthenticator = new BricklinkWebAuthenticator();
        BricklinkSession bricklinkSession = bricklinkWebAuthenticator.authenticate("tvattima", "N1njago!");

        assertThat(bricklinkSession).isNotNull();
    }

}