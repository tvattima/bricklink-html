package com.bricklink.api.html.model.v2;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@Setter
@Getter
@EqualsAndHashCode
@ToString
public class CatalogItem {
    public static final CatalogItem EMPTY = new CatalogItem();

    Integer itemId;
    String itemNo;
    String itemName;
    String itemType;

    public double itemNameMatch(String itemName) {
        boolean match = true;
        int matchingWordCount = 0;
        String[] itemNameWords = Optional.ofNullable(itemName).map(s -> s.split(" ")).orElse(new String[]{});
        int totalWords = itemNameWords.length;
        for (String itemNameWord : itemNameWords) {
            if (StringUtils.containsIgnoreCase(itemName, itemNameWord)) {
                matchingWordCount++;
            }
        }
        return (totalWords > 0) ? ((double) matchingWordCount / (double) totalWords) : 0.0d;
    }
}
