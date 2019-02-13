package com.bricklink.api.html.support;

import com.bricklink.api.html.model.v2.CatalogItem;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CatalogItemResponseAdapterTest {

    @Test
    public void extractMetaDescriptionFields() {
        CatalogItemResponseAdapter catalogItemResponseAdapter = new CatalogItemResponseAdapter();
        CatalogItem catalogItem = new CatalogItem();
        String metaDescription = "ItemName: Lego Main Street, ItemType: Set, ItemNo: 6390-1, Buy and sell LEGO parts, Minifigs and sets, both new or used from the world's largest online LEGO marketplace.";
        catalogItemResponseAdapter.extractMetaDescriptionFields(metaDescription, catalogItem);
        assertThat(catalogItem.getItemName()).isEqualTo("Lego Main Street");
        assertThat(catalogItem.getItemType()).isEqualTo("Set");
        assertThat(catalogItem.getItemNo()).isEqualTo("6390-1");
    }
}