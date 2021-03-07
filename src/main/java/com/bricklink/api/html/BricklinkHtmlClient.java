package com.bricklink.api.html;

import com.bricklink.api.html.model.v2.CatalogItem;
import com.bricklink.api.html.model.v2.WantedList;
import com.bricklink.web.model.AuthenticationResult;
import feign.Param;
import feign.RequestLine;

public interface BricklinkHtmlClient {
    @RequestLine("GET /v2/catalog/catalogitem.page?S={itemNumber}")
    CatalogItem getCatalogSetItemId(@Param("itemNumber") String itemNumber);

    @RequestLine("GET /v2/catalog/catalogitem.page?G={itemNumber}")
    CatalogItem getCatalogGearItemId(@Param("itemNumber") String itemNumber);

    @RequestLine("GET /v2/catalog/catalogitem.page?B={itemNumber}")
    CatalogItem getCatalogBookItemId(@Param("itemNumber") String itemNumber);
}
