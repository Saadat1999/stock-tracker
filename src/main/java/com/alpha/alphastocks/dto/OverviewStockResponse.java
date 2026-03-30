package com.alpha.alphastocks.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OverviewStockResponse(String symbol,
                                    String asset,
                                    String name,
                                    String description,
                                    String sector,
                                    String industry,
                                    String marketCapt,
                                    String dividendYield,
                                    String dividendDate) {
}
