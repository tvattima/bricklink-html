package com.bricklink.web.support;

import com.bricklink.web.BricklinkWebException;
import com.bricklink.web.configuration.BricklinkWebProperties;
import com.bricklink.web.model.AuthenticationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class BricklinkWebService {
    private final BricklinkWebProperties properties;
    private final ObjectMapper objectMapper;

    public BricklinkSession uploadInventoryImage(BricklinkSession bricklinkSession, Long blInventoryId, Path imagePath) {
        URL imgAddURL = properties.getURL("imgAdd");
        WebClient webClient = bricklinkSession.getWebClient();
        String imageAddPage = String.format(imgAddURL.toExternalForm(), blInventoryId);
        log.debug("Getting image add page [{}]", imgAddURL);
        try {
            HtmlPage uploadImagePage = webClient.getPage(imageAddPage);
            List<HtmlForm> forms = uploadImagePage.getForms();

            forms.forEach(f -> {
                if (f.getActionAttribute()
                     .startsWith("imgAdd.asp")) {
                    log.info("Uploading Image [{}] for inventoryId [{}] to URL [{}]", imagePath, blInventoryId, imageAddPage);
                    f.getInputByName("FILE")
                     .setValueAttribute(imagePath.toString());
                }
            });
        } catch (IOException e) {
            throw new BricklinkWebException(e);
        }
        return bricklinkSession;
    }

    public BricklinkSession authenticate() {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions()
                 .setRedirectEnabled(true);
        webClient.getOptions()
                 .setThrowExceptionOnFailingStatusCode(false);
        BricklinkSession bricklinkSession = new BricklinkSession(webClient);
        try {
            URL loginPrepURL = properties.getURL("login-prep");
            log.debug("Getting login-prep page [{}]", loginPrepURL);
            webClient.getPage(loginPrepURL);

            URL loginLogoutURL = properties.getURL("login-logout");
            WebRequest loginRequest = new WebRequest(loginLogoutURL, HttpMethod.POST);
            Cookie blckMID = webClient.getCookieManager()
                                      .getCookie("blckMID");
            loginRequest.setRequestParameters(new ArrayList<>());
            loginRequest.getRequestParameters()
                        .add(new NameValuePair("userid", properties.getBricklink()
                                                                   .getCredential()
                                                                   .getUsername()));
            loginRequest.getRequestParameters()
                        .add(new NameValuePair("password", properties.getBricklink()
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
            URL loginLogoutURL = properties.getURL("login-logout");
            log.debug("Logging out [{}]", loginLogoutURL);
            bricklinkSession.getWebClient()
                            .getPage("https://www.bricklink.com/ajax/renovate/loginandout.ajax?do_logout=true");
        } catch (IOException e) {
            throw new BricklinkWebException(e);
        }
        return bricklinkSession;
    }
}
