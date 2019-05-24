package com.bricklink.web.configuration;

import com.bricklink.web.BricklinkWebException;
import com.bricklink.web.model.AuthenticationResult;
import com.bricklink.web.support.BricklinkSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

@Slf4j
@RequiredArgsConstructor
public class BricklinkSessionPooledObjectFactory extends BasePooledObjectFactory<BricklinkSession> {
    private final BricklinkWebProperties bricklinkWebProperties;
    private final ObjectMapper objectMapper;

    @Override
    public BricklinkSession create() throws Exception {
        return authenticate();
    }

    @Override
    public PooledObject<BricklinkSession> wrap(BricklinkSession bricklinkSession) {
        return new DefaultPooledObject<>(bricklinkSession);
    }

    @Override
    public void destroyObject(PooledObject<BricklinkSession> p) throws Exception {
        logout(p.getObject());
        super.destroyObject(p);
    }

    public BricklinkSession authenticate() {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions()
                .setRedirectEnabled(true);
        webClient.getOptions()
                .setThrowExceptionOnFailingStatusCode(false);
        BricklinkSession bricklinkSession = new BricklinkSession(webClient);
        try {
            URL loginPrepURL = bricklinkWebProperties.getURL("login-prep");
            log.debug("Getting login-prep page [{}]", loginPrepURL);
            webClient.getPage(loginPrepURL);

            URL loginLogoutURL = bricklinkWebProperties.getURL("login-logout");
            WebRequest loginRequest = new WebRequest(loginLogoutURL, HttpMethod.POST);
            Cookie blckMID = webClient.getCookieManager()
                    .getCookie("blckMID");
            loginRequest.setRequestParameters(new ArrayList<>());
            loginRequest.getRequestParameters()
                    .add(new NameValuePair("userid", bricklinkWebProperties.getBricklink()
                            .getCredential()
                            .getUsername()));
            loginRequest.getRequestParameters()
                    .add(new NameValuePair("password", bricklinkWebProperties.getBricklink()
                            .getCredential()
                            .getPassword()));
            loginRequest.getRequestParameters()
                    .add(new NameValuePair("override", "false"));
            loginRequest.getRequestParameters()
                    .add(new NameValuePair("keepme_loggedin", "false"));
            loginRequest.getRequestParameters()
                    .add(new NameValuePair("mid", blckMID.getValue()));
            loginRequest.getRequestParameters()
                    .add(new NameValuePair("pageid", "LOGIN"));
            log.debug("Submitting login [{}]", loginLogoutURL);
            WebResponse response = webClient.loadWebResponse(loginRequest);
            AuthenticationResult authenticationResult = objectMapper.readValue(response.getContentAsString(), AuthenticationResult.class);
            bricklinkSession.setAuthenticationResult(authenticationResult);
            if (authenticationResult.getReturnCode() == 0) {
                log.info("Bricklink Authentication successful | user_no [{}], user_id [{}], user_name", authenticationResult.getUser()
                        .getUser_no(), authenticationResult.getUser()
                        .getUser_id(), authenticationResult.getUser()
                        .getUser_name());
            } else {
                throw new BricklinkWebException(String.format("Authentication Failed [%d] - [%s]", authenticationResult.getReturnCode(), authenticationResult.getReturnMessage()));
            }
        } catch (IOException e) {
            throw new BricklinkWebException(e);
        }
        return bricklinkSession;
    }

    public BricklinkSession logout(BricklinkSession bricklinkSession) {
        try {
            URL loginLogoutURL = bricklinkWebProperties.getURL("login-logout");
            log.debug("Logging out [{}]", loginLogoutURL);
            bricklinkSession.getWebClient()
                    .getPage("https://www.bricklink.com/ajax/renovate/loginandout.ajax?do_logout=true");
        } catch (IOException e) {
            throw new BricklinkWebException(e);
        }
        return bricklinkSession;
    }
}
