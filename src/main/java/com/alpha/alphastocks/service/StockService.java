package com.alpha.alphastocks.service;

import com.alpha.alphastocks.client.StockClient;
import com.alpha.alphastocks.dto.*;
import com.alpha.alphastocks.entity.FavoriteStock;
import com.alpha.alphastocks.exception.EmptyQuoteException;
import com.alpha.alphastocks.exception.FavoriteAlreadyExistsException;
import com.alpha.alphastocks.exception.MaxRateLimitException;
import com.alpha.alphastocks.exception.NoAlphaResponseException;
import com.alpha.alphastocks.repository.FavoriteStockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class StockService {

    private final StockService self;
    private final StockClient stockClient;
    private final FavoriteStockRepository favoriteStockRepository;

    @Autowired
    public StockService(@Lazy StockService self, StockClient stockClient, FavoriteStockRepository favoriteStockRepository) {
        this.stockClient = stockClient;
        this.favoriteStockRepository = favoriteStockRepository;
        this.self = self;
    }

    @Cacheable(value = "stocks")
    public StockResponse getStock(String symbol) {
        VantageResponse vantageResponse = stockClient.getStockBySymbol(symbol);
        if(vantageResponse==null) {
            throw new NoAlphaResponseException("No response from Alpha Vantage");
        } else if(vantageResponse.information()!=null && !vantageResponse.information().isBlank()) {
            throw new MaxRateLimitException(vantageResponse.information()+" "+ LocalDate.now());
        } else if(vantageResponse.quote()==null) {
            throw new EmptyQuoteException("Alpha Vantage did not return quote data");
        }

        return StockResponse.builder().
                symbol(vantageResponse.quote().symbol()).
                price(Double.parseDouble(vantageResponse.quote().price())).
                tradingDay(vantageResponse.quote().latestTradingDay()).build();
    }

    public OverviewResponse getOverview(String symbol) {
        return stockClient.getStockOverview(symbol);
    }

    public List<DailyStockResponse> getHistory(String symbol, int days) {
        TimeSeriesResponse response = stockClient.getStockHistory(symbol);

        return response.timeSeries().entrySet().stream().limit(days).
                map(stringTimeSeriesEntry -> {
                    var date = stringTimeSeriesEntry.getKey();
                    var value = stringTimeSeriesEntry.getValue();

                    return new DailyStockResponse(
                            response.metaData().symbol(),
                            date,
                            value.open(),
                            value.close(),
                            Double.parseDouble(value.high()),
                            Double.parseDouble(value.low()),
                            Long.parseLong(value.volume())
                    );
                }).toList();
    }

    @Transactional
    public FavoriteStock addFavorite(String symbol) {
        if(favoriteStockRepository.existsBySymbol(symbol)) {
            throw new FavoriteAlreadyExistsException("Symbol has already been added: "+symbol);
        }
        return favoriteStockRepository.save(FavoriteStock.builder().symbol(symbol).build());
    }

    public List<StockResponse> getFavorites() {
        List<FavoriteStock> favoriteStockList = favoriteStockRepository.findAll();
        return favoriteStockList.stream().map(favoriteStock -> self.getStock(favoriteStock.getSymbol())).toList();
    }
}
