package com.bricklink.api.html.model.v2;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class WantedListPageAggregate {
    List<WantedList> wantedLists;
    Integer totalItems;
    Integer totalCnt;
}
