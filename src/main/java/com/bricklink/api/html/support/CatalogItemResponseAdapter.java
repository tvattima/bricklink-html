package com.bricklink.api.html.support;

import com.bricklink.api.html.model.v2.CatalogItem;
import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.htmlunit.corejs.javascript.NativeObject;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.function.Consumer;

@Component
@Slf4j
public class CatalogItemResponseAdapter implements ResponseAdapter<CatalogItem> {

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
            extractItemData(page, catalogItem);
        }
        return catalogItem;
    }

    @Override
    public Type getType() {
        return CatalogItem.class;
    }

    public void extractItemData(final HtmlPage page, CatalogItem catalogItem) {
        ScriptResult scriptResult = null;
        String javascript = "_var_item";
        try {
            scriptResult = page.executeJavaScript(javascript);
        } catch (Exception e) {
            log.warn("Unable to execute javascript [{}] on page [{}]", javascript, page);
            log.error(e.getMessage(), e);
            return;
        }
        extractItemData(scriptResult, "catID", (value) -> catalogItem.setCatId(Integer.valueOf((String) value)));
        extractItemData(scriptResult, "idItem", (value) -> catalogItem.setItemId(((Double) value).intValue()));
        extractItemData(scriptResult, "itemno", catalogItem::setItemNo);
        extractItemData(scriptResult, "strItemName", catalogItem::setItemName);
        extractItemData(scriptResult, "type", catalogItem::setItemType);
        extractItemData(scriptResult, "typeName", catalogItem::setItemTypeName);
    }

    private <T> void extractItemData(final ScriptResult scriptResult, final String variableName, Consumer<T> catalogItemConsumer) {
        T value = null;
        try {
            value = (T) ((NativeObject) scriptResult.getJavaScriptResult()).get(variableName);
            catalogItemConsumer.accept(value);
        } catch (Exception e) {
            log.warn("Unable to extract item data for variable named [{}] from javascript result [{}]", variableName, scriptResult);
            log.error(e.getMessage(), e);
        }
    }
}
