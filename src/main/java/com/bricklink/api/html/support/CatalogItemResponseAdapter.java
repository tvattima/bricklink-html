package com.bricklink.api.html.support;

import com.bricklink.api.html.model.v2.CatalogItem;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlMeta;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class CatalogItemResponseAdapter implements ResponseAdapter<CatalogItem> {

    private static final Pattern META_PATTERN = Pattern.compile("^ItemName:\\s+?(.+?),\\s+?ItemType:\\s+?(.+?),\\s+?ItemNo:\\s+?(.+?),.*$");

    @Override
    public CatalogItem extract(feign.Response response, Type type) {
        CatalogItem catalogItem = new CatalogItem();
        HtmlUnitBody body = (HtmlUnitBody) response.body();
        HtmlPage page = body.getHtmlPage();
        if (null == page) {
            log.warn("page is null");
        } else {
            if ("BrickLink Page Not Found".equals(page.getTitleText())) {
                return CatalogItem.EMPTY;
            }
            DomElement domElement = page.getElementById("_idAddToWantedLink");
            if (null == domElement) {
                log.warn("domElement is null");
            } else {
                String itemIdString = domElement.getAttribute("data-itemid");
                extractMetaDescriptionFields(page, catalogItem);
                catalogItem.setItemId(Integer.valueOf(itemIdString));
            }
        }
        return catalogItem;
    }

    @Override
    public Type getType() {
        return CatalogItem.class;
    }

    public void extractMetaDescriptionFields(final HtmlPage page, CatalogItem catalogItem) {
        String metaAttributeName = "name";
        String metaAttributeValue = "description";
        List<HtmlElement> metaElements = page.getHead().getElementsByAttribute("meta", metaAttributeName, metaAttributeValue);
        if (metaElements.size() == 1) {
            HtmlMeta htmlMeta = (HtmlMeta)metaElements.get(0);
            String metaDescription = htmlMeta.getContentAttribute();
            extractMetaDescriptionFields(metaDescription, catalogItem);
        } else {
            log.warn("page did not contain a meta tag with attribute [{}] with value [{}]", metaAttributeName, metaAttributeValue);
        }
    }

    public void extractMetaDescriptionFields(final String metaDescription, CatalogItem catalogItem) {
        Matcher matcher = META_PATTERN.matcher(metaDescription);
        if (matcher.matches()) {
            String itemName = matcher.group(1);
            String itemType = matcher.group(2);
            String itemNo = matcher.group(3);
            catalogItem.setItemName(itemName);
            catalogItem.setItemNo(itemNo);
            catalogItem.setItemType(itemType);
        } else {
            log.warn("metaDescription [{}] did not match pattern [{}]", metaDescription, META_PATTERN.pattern());
        }
    }
}
