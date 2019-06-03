package com.bricklink.web.support;

import com.bricklink.web.BricklinkWebException;
import com.bricklink.web.configuration.BricklinkWebProperties;
import com.bricklink.web.model.AuthenticationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

@Slf4j
public class BricklinkWebService {
    private final HttpClientConnectionManager httpClientConnectionManager;
    private final BricklinkWebProperties properties;
    private final ObjectMapper objectMapper;
    private final ConnectionKeepAliveStrategy connectionKeepAliveStrategy;
    private CloseableHttpClient httpClient;
    private BricklinkSession bricklinkSession;

    public BricklinkWebService(HttpClientConnectionManager httpClientConnectionManager, BricklinkWebProperties properties, ObjectMapper objectMapper, ConnectionKeepAliveStrategy connectionKeepAliveStrategy) {
        this.httpClientConnectionManager = httpClientConnectionManager;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.connectionKeepAliveStrategy = connectionKeepAliveStrategy;

        httpClient = HttpClients.custom()
                                .setConnectionManager(httpClientConnectionManager)
                                .setKeepAliveStrategy(connectionKeepAliveStrategy)
                                .build();
        httpClient = HttpClientBuilder.create()
                                      .build();
        this.bricklinkSession = authenticate();
    }

    public BricklinkSession uploadInventoryImage(Long blInventoryId, Path imagePath) {

        // GET imgAdd page
        URL imgAddUrl = properties.getURL("imgAdd");
        String imgAddUrlString = imgAddUrl.toExternalForm() + "?invID=" + blInventoryId;
        HttpUriRequest imgAddGet = new HttpGet(imgAddUrlString);
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(imgAddGet, bricklinkSession.getHttpContext());
            EntityUtils.consume(response.getEntity());
            response.close();
        } catch (IOException e) {
            throw new BricklinkWebException(e);
        }

        // Upload thumbnail photo to inventory item
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

        try {
            byte[] bytes = Files.readAllBytes(imagePath);
            builder.addBinaryBody("FILE", bytes, ContentType.IMAGE_JPEG, imagePath.getFileName()
                                                                                  .toString());
        } catch (IOException e) {
            throw new BricklinkWebException(e);
        }

        HttpPost imaAddPost = new HttpPost(imgAddUrlString + "&a=a");
        imaAddPost.setEntity(builder.build());
        try {
            response = httpClient.execute(imaAddPost, bricklinkSession.getHttpContext());
            EntityUtils.consume(response.getEntity());
            response.close();
        } catch (IOException e) {
            throw new BricklinkWebException(e);
        }
        return bricklinkSession;
    }

    public BricklinkSession authenticate() {
        CookieStore cookieStore = new BasicCookieStore();
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(cookieStore);
        final BricklinkSession bricklinkSession = new BricklinkSession(context);

        // Authenticate
        String username = properties.getBricklink()
                                    .getCredential()
                                    .getUsername();
        String secret = properties.getBricklink()
                                  .getCredential()
                                  .getPassword();
        URL loginUrl = properties.getURL("login-logout");
        CloseableHttpResponse response = null;
        try {
            HttpUriRequest login = RequestBuilder.post()
                                                 .setUri(loginUrl.toURI())
                                                 .addParameter("userid", username)
                                                 .addParameter("password", secret)
                                                 .addParameter("override", "false")
                                                 .addParameter("keepme_loggedin", "false")
                                                 .addParameter("pageid", "LOGIN")
                                                 .addParameter("mid", computeMID())
                                                 .build();
            response = httpClient.execute(login, context);
            AuthenticationResult authenticationResult = objectMapper.readValue(IOUtils.toString(response.getEntity()
                                                                                                        .getContent(), Charset.defaultCharset()), AuthenticationResult.class);
            EntityUtils.consume(response.getEntity());
            response.close();
            bricklinkSession.setAuthenticationResult(authenticationResult);
            if (authenticationResult.getReturnCode() == 0) {
                log.info("Bricklink Authentication successful | user_no [{}], user_id [{}], user_name", authenticationResult.getUser()
                                                                                                                            .getUser_no(), authenticationResult.getUser()
                                                                                                                                                               .getUser_id(), authenticationResult.getUser()
                                                                                                                                                                                                  .getUser_name());
            } else {
                log.error("Authentication Failed [{}] - [{}]", authenticationResult.getReturnCode(), authenticationResult.getReturnMessage());
                throw new BricklinkWebException(String.format("Authentication Failed [%d] - [%s]", authenticationResult.getReturnCode(), authenticationResult.getReturnMessage()));
            }
        } catch (IOException | URISyntaxException e) {
            throw new BricklinkWebException(e);
        }
        return bricklinkSession;
    }

    public BricklinkSession logout() {
        URL logoutUrl = properties.getURL("login-logout");
        String logoutUrlString = logoutUrl.toExternalForm() + "?do_logout=true";
        HttpGet logout = new HttpGet(logoutUrlString);
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(logout, bricklinkSession.getHttpContext());
            System.out.println(IOUtils.toString(response.getEntity()
                                                        .getContent(), Charset.defaultCharset()));
            EntityUtils.consume(response.getEntity());
            response.close();
        } catch (IOException e) {
            throw new BricklinkWebException(e);
        }
        return bricklinkSession;
    }

    public String computeMID() {
        Random r = new Random();
        Long systemTimeMillis = System.currentTimeMillis();
        String hexSystemTimeMillis = Long.toHexString(systemTimeMillis);
        String paddedHexSystemTimeMillis = StringUtils.rightPad(hexSystemTimeMillis, 16, '0');
        String mid = paddedHexSystemTimeMillis + "-" + StringUtils.leftPad(Integer.toHexString(r.nextInt(65535) + 1), 4, '0') + StringUtils.leftPad(Integer.toHexString(r.nextInt(65535) + 1), 4, '0') + StringUtils.leftPad(Integer.toHexString(r.nextInt(65535) + 1), 4, '0') + StringUtils.leftPad(Integer.toHexString(r.nextInt(65535) + 1), 4, '0');
        return mid;
    }
}
