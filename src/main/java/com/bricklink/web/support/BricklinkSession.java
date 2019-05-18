package com.bricklink.web.support;

import com.bricklink.web.model.AuthenticationResult;
import com.gargoylesoftware.htmlunit.WebClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class BricklinkSession {
    private final WebClient webClient;

    @Setter
    private AuthenticationResult authenticationResult;
}
