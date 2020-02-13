package com.bricklink.web.support;

import com.bricklink.web.BricklinkWebException;
import com.bricklink.web.configuration.BricklinkWebProperties;
import com.bricklink.web.model.AuthenticationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class BricklinkWebService {
    private final HttpClientConnectionManager httpClientConnectionManager;
    private final BricklinkWebProperties properties;
    private final ObjectMapper objectMapper;
    private final ConnectionKeepAliveStrategy connectionKeepAliveStrategy;
    private CloseableHttpClient httpClient;
    private BricklinkSession bricklinkSession;
    private RequestConfig requestConfig = RequestConfig.custom()
                                                       .setCookieSpec(CookieSpecs.STANDARD)
                                                       .build();

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
        HttpGet imgAddGet = new HttpGet(imgAddUrlString);
        imgAddGet.setConfig(requestConfig);
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
        imaAddPost.setConfig(requestConfig);
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
                                                 .setConfig(requestConfig)
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
        logout.setConfig(requestConfig);
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

    public HttpClient getHttpClient() {
        return this.httpClient;
    }

    public BricklinkSession updateInventoryCondition(Long blInventoryId, String invNew, String invComplete) {
        log.info("Starting update of Bricklink inventory [{}] condition...", blInventoryId);
        // GET Inventory Detail page (contains form to update)
        URL inventoryDetailUrl = properties.getURL("inventoryDetail");
        String inventoryDetailUrlString = inventoryDetailUrl.toExternalForm() + "?invID=" + blInventoryId;
        HttpGet inventoryDetailGet = new HttpGet(inventoryDetailUrlString);
        inventoryDetailGet.setConfig(requestConfig);
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(inventoryDetailGet, bricklinkSession.getHttpContext());
            EntityUtils.consume(response.getEntity());
            response.close();
        } catch (IOException e) {
            throw new BricklinkWebException(e);
        }

        // Update form fields
        URL inventoryUpdateUrl = properties.getURL("inventoryUpdate");
        String inventoryUpdateUrlString = inventoryUpdateUrl.toExternalForm() + "?invID=" + blInventoryId + "&pg=1&invSearch=D&a=c&viewPriceGuide=";
        response = null;
        try {
            inventoryUpdateUrl = new URL(inventoryUpdateUrlString);
            RequestBuilder requestBuilder = RequestBuilder.post()
                                                          .setUri(inventoryUpdateUrl.toURI());
            setInventoryUpdateFormFieldsForConditionUpdate(requestBuilder, blInventoryId, invNew, invComplete);
            HttpUriRequest inventoryUpdateRequest = requestBuilder.setConfig(requestConfig)
                                                                  .build();
            // POST Inventory Update
            response = httpClient.execute(inventoryUpdateRequest, bricklinkSession.getHttpContext());
            EntityUtils.consume(response.getEntity());
            response.close();
        } catch (IOException | URISyntaxException e) {
            throw new BricklinkWebException(e);
        }
        log.info("Updated Bricklink inventory [{}] Condition New/Used to [{}], Completeness to [{}]", blInventoryId, invNew, invComplete);

        return bricklinkSession;
    }

    public BricklinkSession updateExtendedDescription(Long blInventoryId, String extendedDescription) {
        log.info("Starting update of Bricklink inventory [{}] extended description...", blInventoryId);
        // GET Inventory Detail page (contains form to update)
        URL inventoryDetailUrl = properties.getURL("inventoryDetail");
        String inventoryDetailUrlString = inventoryDetailUrl.toExternalForm() + "?invID=" + blInventoryId;
        HttpGet inventoryDetailGet = new HttpGet(inventoryDetailUrlString);
        inventoryDetailGet.setConfig(requestConfig);
        CloseableHttpResponse response = null;

        String oldItemType = null;
        String oldItemNoSeq = null;
        String oldColorID = null;
        String oldCatID = null;

        try {
            response = httpClient.execute(inventoryDetailGet, bricklinkSession.getHttpContext());
            String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            oldItemType = extractFromPattern(content, Pattern.compile("<INPUT TYPE=\"HIDDEN\" NAME=\"oldItemType"+blInventoryId+"\" VALUE=\"(.*?)\">"));
            oldItemNoSeq = extractFromPattern(content, Pattern.compile("<INPUT TYPE=\"HIDDEN\" NAME=\"oldItemNoSeq"+blInventoryId+"\" VALUE=\"(.*?)\">"));
            oldColorID = extractFromPattern(content, Pattern.compile("<INPUT TYPE=\"HIDDEN\" NAME=\"oldColorID"+blInventoryId+"\" VALUE=\"(.*?)\">"));
            oldCatID = extractFromPattern(content, Pattern.compile("<INPUT TYPE=\"HIDDEN\" NAME=\"oldCatID"+blInventoryId+"\" VALUE=\"(.*?)\">"));
            EntityUtils.consume(response.getEntity());
            response.close();
        } catch (IOException e) {
            throw new BricklinkWebException(e);
        }

        // Update form fields
        URL inventoryUpdateUrl = properties.getURL("inventoryUpdate");
        String inventoryUpdateUrlString = inventoryUpdateUrl.toExternalForm() + "?invID=" + blInventoryId + "&pg=1&invSearch=D&a=c&viewPriceGuide=";
        response = null;
        try {
            inventoryUpdateUrl = new URL(inventoryUpdateUrlString);
            RequestBuilder requestBuilder = RequestBuilder.post()
                                                          .setUri(inventoryUpdateUrl.toURI());
            setInventoryUpdateFormFieldsForExtendedDescriptionUpdate(requestBuilder, blInventoryId, extendedDescription);
            addOldNewFormField(requestBuilder, blInventoryId, "ItemType", oldItemType, oldItemType);
            addOldNewFormField(requestBuilder, blInventoryId, "ItemNoSeq", oldItemNoSeq, oldItemNoSeq);
            addOldNewFormField(requestBuilder, blInventoryId, "ColorID", oldColorID, oldColorID);
            addOldNewFormField(requestBuilder, blInventoryId, "CatID", oldCatID, oldCatID);
            HttpUriRequest inventoryUpdateRequest = requestBuilder.setConfig(requestConfig)
                                                                  .build();
            // POST Inventory Update
            response = httpClient.execute(inventoryUpdateRequest, bricklinkSession.getHttpContext());
            EntityUtils.consume(response.getEntity());
            response.close();
        } catch (IOException | URISyntaxException e) {
            throw new BricklinkWebException(e);
        }
        log.info("Updated Bricklink inventory [{}] Extended Description to [{}]", blInventoryId, extendedDescription);

        return bricklinkSession;
    }

    private void addOldNewFormField(RequestBuilder requestBuilder, Long inventoryId, String formFieldName, String oldValue, String newValue) {
        requestBuilder.addParameter("old" + formFieldName+inventoryId, oldValue);
        requestBuilder.addParameter("new" + formFieldName+inventoryId, newValue);
    }

    private void addPlaceholderOldNewFormField(RequestBuilder requestBuilder, Long inventoryId, String formFieldName) {
        addOldNewFormField(requestBuilder, inventoryId, formFieldName, "x", "x");
    }

    private void setInventoryUpdateFormFieldsForConditionUpdate(RequestBuilder requestBuilder, Long inventoryId, String invNew, String invComplete) {
        requestBuilder.addParameter("invID", Long.toString(inventoryId));
        requestBuilder.addParameter("revID", "1");
        requestBuilder.addParameter("userID", Integer.toString(bricklinkSession.getAuthenticationResult()
                                                                               .getUser()
                                                                               .getUser_no()));
        requestBuilder.addParameter("oldItemNotCatalog", "");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "InvDescription");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "InvExtended");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "InvRemarks");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "InvQty");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "InvPrice");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "InvCost");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "InvBulk");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "InvSale");
        addOldNewFormField(requestBuilder, inventoryId, "InvNew", "Q", invNew);
        addOldNewFormField(requestBuilder, inventoryId, "InvComplete", "Q", invComplete);
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "TierQty1");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "TierPrice1");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "InvRetain");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "TierQty2");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "TierPrice2");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "InvStock");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "InvStockID");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "TierQty3");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "TierPrice3");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "InvBuyerUsername");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "ItemType");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "ItemNoSeq");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "ColorID");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "CatID");
    }

    private void setInventoryUpdateFormFieldsForExtendedDescriptionUpdate(RequestBuilder requestBuilder, Long inventoryId, String invExtended) {
        requestBuilder.addParameter("invID", Long.toString(inventoryId));
        requestBuilder.addParameter("revID", "1");
        requestBuilder.addParameter("userID", Integer.toString(bricklinkSession.getAuthenticationResult()
                                                                               .getUser()
                                                                               .getUser_no()));
        requestBuilder.addParameter("oldItemNotCatalog", "");
        addOldNewFormField(requestBuilder, inventoryId, "InvExtended", "Q", invExtended);
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "InvRemarks");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "InvQty");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "InvPrice");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "InvCost");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "InvBulk");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "InvSale");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "InvNew");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "InvComplete");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "TierQty1");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "TierPrice1");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "InvRetain");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "TierQty2");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "TierPrice2");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "InvStock");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "InvStockID");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "TierQty3");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "TierPrice3");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "InvBuyerUsername");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "ItemType");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "ItemNoSeq");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "ColorID");
        addPlaceholderOldNewFormField(requestBuilder, inventoryId, "CatID");
    }

    public String extractFromPattern(String content, Pattern pattern) {
        String extracted = null;
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            extracted = matcher.group(1);
        }
        return extracted;
    }
}
