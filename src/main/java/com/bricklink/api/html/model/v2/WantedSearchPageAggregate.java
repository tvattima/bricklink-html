package com.bricklink.api.html.model.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class WantedSearchPageAggregate {
    Integer totalResults;
    List<WantedList> lists;
    Set<WantedItem> wantedItems;
    Integer totalCnt;
    WantedList wantedListInfo;
}
