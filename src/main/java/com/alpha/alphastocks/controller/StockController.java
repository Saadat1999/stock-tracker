package com.alpha.alphastocks.controller;

import com.alpha.alphastocks.dto.DailyStockResponse;
import com.alpha.alphastocks.dto.FavoriteStockRequest;
import com.alpha.alphastocks.dto.OverviewResponse;
import com.alpha.alphastocks.dto.StockResponse;
import com.alpha.alphastocks.entity.FavoriteStock;
import com.alpha.alphastocks.service.StockService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/stocks")
@RequiredArgsConstructor
public class StockController {
    private final StockService stockService;

    @GetMapping("/{stockSymbol}")
    public StockResponse getStock(@PathVariable("stockSymbol") @NonNull String stockSymbol) {
        return stockService.getStock(stockSymbol.toUpperCase());
    }

    @GetMapping({"/{stockSymbol}/overview"})
    public OverviewResponse getOverview(@PathVariable("stockSymbol") @NonNull String stockSymbol) {
        return stockService.getOverview(stockSymbol.toUpperCase());
    }

    @GetMapping("/{stockSymbol}/history")
    public List<DailyStockResponse> getStockHistory(@PathVariable("stockSymbol") @NonNull String symbol,
                                                    @RequestParam(defaultValue = "30") int days) {
        return stockService.getHistory(symbol.toUpperCase(), days);
    }

    @PostMapping("/favorites")
    public ResponseEntity<FavoriteStock> addToFavorites(@RequestBody @NonNull FavoriteStockRequest request) {
        FavoriteStock favoriteStock = stockService.addFavorite(request.symbol().toUpperCase());
        return ResponseEntity.ok(favoriteStock);
    }

    @GetMapping("/favorites")
    public List<StockResponse> getFavoriteStocks() {
        return stockService.getFavorites();
    }
}
