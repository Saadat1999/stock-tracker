package com.alpha.alphastocks.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record VantageResponse(@JsonProperty("Global Quote") GlobalQuote quote,
                              @JsonProperty("Information") String information) {

    public record GlobalQuote(
            @JsonProperty("01. symbol") String symbol,
            @JsonProperty("05. price") String price,
            @JsonProperty("07. latest trading day") String latestTradingDay
    ) {}

}
