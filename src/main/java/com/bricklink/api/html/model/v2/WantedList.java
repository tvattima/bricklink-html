package com.bricklink.api.html.model.v2;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

@Setter
@Getter
@EqualsAndHashCode
@ToString
public class WantedList {
    public static final WantedList EMPTY = new WantedList();

    Integer totalResults;
    Integer totalCnt;
    List<WantedListInfo> lists;
    List<WantedItem> wantedItems;
    List<CategoryType> categoryTypes;
    List<Duplicate> duplicates;
    WantedListInfo wantedListInfo;
    Integer searchMode;
    Integer emptySearch;
    Integer showAdv;

    @Setter
    @Getter
    @EqualsAndHashCode
    @ToString
    static class WantedListInfo {
        String name;
        String desc;
        Integer num;
        Integer totalNum;
        Long id;
        String curSymbol;
        Double progress;
    }

    @Setter
    @Getter
    @EqualsAndHashCode
    @ToString
    static class WantedItem {
        Long wantedId;
        Long wantedMoreId;
        String wantedMoreName;
        String itemNo;
        String itemId;
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
        Integer colorId;
        String colorName;
        String colorHex;
    }

    @Setter
    @Getter
    @EqualsAndHashCode
    @ToString
    static class CategoryType {
        String type;
        Integer total;
        List<Category> categories;
    }

    @Setter
    @Getter
    @EqualsAndHashCode
    @ToString
    static class Category {
        String catName;
        Integer catId;
        Integer cnt;
        Integer invCnt;
    }

    @Setter
    @Getter
    @EqualsAndHashCode
    @ToString
    static class Duplicate {
    }
}
