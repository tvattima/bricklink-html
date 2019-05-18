package com.bricklink.web.support;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class BricklinkWebAuthenticator {
    public BricklinkSession authenticate(String username, String password) throws Exception {
//        WebClient webClient = new WebClient(BrowserVersion.CHROME);
//        HtmlPage mainPage = (HtmlPage)webClient.getPage("https://www.bricklink.com/v2/main.page");
//        HtmlButton showLoginButton = (HtmlButton)mainPage.getHtmlElementById("nav-login-button");
//        HtmlPage loginPage = showLoginButton.click();
//
//        System.out.println(loginPage.asText());
//        HtmlForm form = loginPage.getHtmlElementById("frmLogin");
//        form.getInputByName("frmUsername").setValueAttribute(username);
//        HtmlInput passWordInput = form.getInputByName("frmPassword");
//        passWordInput.setValueAttribute(password);
//        HtmlButton submitButton = (HtmlButton)loginPage.getHtmlElementById("blbtnLogin");
//        Page response = submitButton.click();
//        System.out.println(response.getWebResponse().getContentAsString());
        return new BricklinkSession();
    }
}
