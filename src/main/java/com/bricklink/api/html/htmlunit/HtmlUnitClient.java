package com.bricklink.api.html.htmlunit;

import com.bricklink.api.html.support.HtmlUnitBody;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import feign.Client;
import feign.Request;
import feign.Response;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class HtmlUnitClient implements Client {
    final WebClient webClient = new WebClient();

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setUseInsecureSSL(true);
        WebRequest webRequest = toWebClientRequest(request);
        HtmlPage htmlPage = webClient.getPage(webRequest);
        return toFeignResponse(request, htmlPage);
    }

    private Response toFeignResponse(Request request, HtmlPage htmlPage) {
        WebResponse webResponse = htmlPage.getWebResponse();
        return feign.Response.builder()
                             .request(request)
                             .status(webResponse.getStatusCode())
                             .reason(webResponse.getStatusMessage())
                             .headers(toMap(webResponse.getResponseHeaders()))
                             .body(new HtmlUnitBody(htmlPage))
                             .build();
    }

    static WebRequest toWebClientRequest(feign.Request input) {
        try {
            return new WebRequest(new URL(input.url()));
        } catch (MalformedURLException e) {
            throw new HtmlUnitException(e);
        }
    }

    public static Map<String, Collection<String>> toMap(List<NameValuePair> headers) {
        Map<String, Collection<String>> headersMap = new HashMap<>();
        for (NameValuePair nvp : headers) {
            String k = nvp.getName();
            String v = nvp.getValue();
            Collection<String> values = Optional.ofNullable(headersMap.get(k))
                                                .orElse(new ArrayList<>());
            values.add(v);
            headersMap.put(k, values);
        }
        return headersMap;
    }
}
