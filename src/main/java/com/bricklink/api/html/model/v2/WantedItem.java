package com.bricklink.api.html.model.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class WantedItem {
    @EqualsAndHashCode.Include
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
