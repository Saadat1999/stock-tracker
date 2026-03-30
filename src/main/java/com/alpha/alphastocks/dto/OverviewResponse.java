package com.alpha.alphastocks.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OverviewResponse(@JsonProperty("Symbol") String symbol,
                               @JsonProperty("AssetType") String asset,
                               @JsonProperty("Name") String name,
                               @JsonProperty("Description") String description,
                               @JsonProperty("Sector") String sector,
                               @JsonProperty("Industry") String industry,
                               @JsonProperty("MarketCapitalization") String marketCapt,
                               @JsonProperty("DividendYield") String dividendYield,
                               @JsonProperty("DividendDate") String dividendDate
                               ) {
}
