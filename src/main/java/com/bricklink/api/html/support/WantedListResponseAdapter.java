package com.bricklink.api.html.support;

import com.bricklink.api.html.model.v2.WantedList;
import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
@Slf4j
public class WantedListResponseAdapter implements ResponseAdapter<WantedList> {
    @Override
    public WantedList extract(Response response, Type type) {
        WantedList wantedList = new WantedList();
        HtmlUnitBody body = (HtmlUnitBody) response.body();
        HtmlPage page = body.getHtmlPage();
        if (null == page) {
            log.warn("page is null");
        } else {
            if ("BrickLink Page Not Found".equals(page.getTitleText())) {
                return wantedList;
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
        return wantedList;
    }

    @Override
    public Type getType() {
        return WantedList.class;
    }
}
