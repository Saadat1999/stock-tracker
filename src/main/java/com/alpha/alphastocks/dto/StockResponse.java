package com.alpha.alphastocks.dto;

import lombok.Builder;

@Builder
public record StockResponse(String symbol, double price, String tradingDay) {
}
