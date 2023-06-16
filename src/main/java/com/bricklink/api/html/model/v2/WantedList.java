package com.bricklink.api.html.model.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
public class WantedList {
    Integer totalResults;
    List<WantedListInfo> lists;
    List<WantedItem> wantedItems;
    List<CategoryType> categories;
    Integer totalCnt;
    WantedListInfo wantedListInfo;
    List<Duplicate> duplicates;
    Integer searchMode;
    Integer emptySearch;
    Integer showAdv;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    static class WantedListInfo {
        Long id;
        String name;
        String desc;
        Integer num;
        Integer totalNum;
        String curSymbol;
        Double progress;
    }

    @Data
    static class WantedItem {
        Long wantedID;
        Long wantedMoreID;
        String wantedMoreName;
        String itemNo;
        String itemID;
        Integer itemSeq;
        String itemName;
        String itemType;
        String imgURL;
        Integer wantedQty;
        Integer wantedQtyFilled;
        String wantedNew;
        String wantedNotify;
        String wantedRemark;
        Double wantedPrice;
        String formatWantedPrice;
        Integer colorID;
        String colorName;
        String colorHex;
    }

    @Data
    static class CategoryType {
        String type;
        List<Category> cats;
        Integer total;
    }

    @Data
    static class Category {
        String catName;
        Integer catID;
        Integer cnt;
        Integer invCnt;
    }

    @Data
    static class Duplicate {
    }
}
