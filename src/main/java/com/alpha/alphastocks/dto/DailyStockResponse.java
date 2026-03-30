package com.alpha.alphastocks.dto;

import lombok.Builder;

@Builder
public record DailyStockResponse(String symbol, String date, String close, String open, double high, double low, long volume) {
}
