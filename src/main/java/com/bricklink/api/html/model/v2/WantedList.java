package com.bricklink.api.html.model.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class WantedList {
    Long id;
    String name;
    String desc;
    Double filledPct;
    Integer num;
    Integer totalNum;
    Integer totalLeft;
    String curSymbol;
    List<WantedItem> items;
}
