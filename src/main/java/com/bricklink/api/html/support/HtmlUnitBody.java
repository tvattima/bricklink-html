package com.bricklink.api.html.support;

import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import feign.Response;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

@Slf4j
@Getter
@Setter
public class HtmlUnitBody implements Response.Body {
    private final WebResponse response;
    private final HtmlPage htmlPage;

    public HtmlUnitBody(HtmlPage htmlPage) {
        this.htmlPage = htmlPage;
        this.response = htmlPage.getWebResponse();
    }

    @Override
    public Integer length() {
        return (int)this.response.getContentLength();
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public InputStream asInputStream() throws IOException {
        return this.response.getContentAsStream();
    }

    @Override
    public Reader asReader() throws IOException {
        return new InputStreamReader(this.asInputStream());
    }

    @Override
    public void close() throws IOException {
        this.htmlPage.cleanUp();
        this.response.cleanUp();
    }
}
