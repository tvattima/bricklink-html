package com.bricklink.api.html.support;

import com.bricklink.api.html.model.v2.WantedSearchPageAggregate;
import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
@Slf4j
public class WantedListResponseAdapter implements ResponseAdapter<WantedSearchPageAggregate> {
    @Override
    public WantedSearchPageAggregate extract(Response response, Type type) {
        WantedSearchPageAggregate wantedSearchPageAggregate = new WantedSearchPageAggregate();
        HtmlUnitBody body = (HtmlUnitBody) response.body();
        HtmlPage page = body.getHtmlPage();
        if (null == page) {
            log.warn("page is null");
        } else {
            if ("BrickLink Page Not Found".equals(page.getTitleText())) {
                return wantedSearchPageAggregate;
            }
            ScriptResult scriptResult = page.executeJavaScript("wlJson");
            log.info(scriptResult.toString());
            DomElement domElement = page.getElementById("_idAddToWantedLink");
            if (null == domElement) {
                log.warn("domElement is null");
            } else {
                String itemIdString = domElement.getAttribute("data-itemid");
//                extractMetaDescriptionFields(page, catalogItem);
//                catalogItem.setItemId(Integer.valueOf(itemIdString));
            }
        }
        return wantedSearchPageAggregate;
    }

    @Override
    public Type getType() {
        return WantedSearchPageAggregate.class;
    }
}
