package com.bricklink.api.html.model.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class WantedItem {
    @EqualsAndHashCode.Include
    String itemID;
    @EqualsAndHashCode.Include
    Integer colorID;
    @EqualsAndHashCode.Include
    String wantedNew;

    Long wantedMoreID;
    String wantedMoreName;
    Long wantedID;
    String itemNo;
    Integer itemSeq;
    String itemName;
    String itemType;
    String imgURL;
    Integer wantedQty;
    Integer wantedQtyFilled;
    String wantedNotify;
    String wantedRemark;
    Double wantedPrice;
    String formatWantedPrice;
    String colorName;
    String colorHex;
}
