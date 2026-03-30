package com.alpha.alphastocks.client;

import com.alpha.alphastocks.dto.OverviewResponse;
import com.alpha.alphastocks.dto.TimeSeriesResponse;
import com.alpha.alphastocks.dto.VantageResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class StockClient {

    private final WebClient webClient;

    @Value("${av_api_key}")
    String apiKey;

    public StockClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public VantageResponse getStockBySymbol(String symbol) {
        return webClient.get().uri(uriBuilder -> uriBuilder.
                queryParam("function", "GLOBAL_QUOTE").
                queryParam("symbol", symbol).
                queryParam("apikey", apiKey).build()).
                retrieve().bodyToMono(VantageResponse.class).block();
    }

    public OverviewResponse getStockOverview(String symbol) {
        return webClient.get().uri(uriBuilder -> uriBuilder.
                queryParam("function", "OVERVIEW").
                queryParam("symbol", symbol).
                queryParam("apikey", apiKey).build()).retrieve().bodyToMono(OverviewResponse.class).block();
    }

    public TimeSeriesResponse getStockHistory(String symbol) {
        return webClient.get().uri(uriBuilder -> uriBuilder.
                queryParam("function", "TIME_SERIES_DAILY").
                queryParam("symbol", symbol).
                queryParam("apikey", apiKey).build()).retrieve().bodyToMono(TimeSeriesResponse.class).block();
    }
}
