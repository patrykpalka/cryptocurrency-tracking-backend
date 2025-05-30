package com.patrykpalka.portfolio.cryptotracker.backend.controller;

import com.patrykpalka.portfolio.cryptotracker.backend.dto.CoinMarketDataResponseDTO;
import com.patrykpalka.portfolio.cryptotracker.backend.dto.CoinPriceResponseDTO;
import com.patrykpalka.portfolio.cryptotracker.backend.dto.CoinsListDTO;
import com.patrykpalka.portfolio.cryptotracker.backend.dto.CryptoPricesResponseDTO;
import com.patrykpalka.portfolio.cryptotracker.backend.service.CryptoService;
import com.patrykpalka.portfolio.cryptotracker.backend.validation.ValidCurrency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/crypto")
public class CryptoController {

    private final CryptoService cryptoService;
    private static final Logger LOGGER = LoggerFactory.getLogger(CryptoController.class);

    public CryptoController(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @GetMapping("/prices")
    private ResponseEntity<List<CryptoPricesResponseDTO>> getCryptoPrices(
            @RequestParam(required = false) String symbols,
            @RequestParam(required = false, defaultValue = "usd") @ValidCurrency String currency
    ) {
        List<CryptoPricesResponseDTO> pricesList = cryptoService.getCryptoPrices(symbols, currency);
        return ResponseEntity.ok(pricesList);
    }

    @GetMapping("/supported")
    private ResponseEntity<List<CoinsListDTO>> getCoinsList() {
        List<CoinsListDTO> coinsList = cryptoService.getCoinsList();
        return ResponseEntity.ok(coinsList);
    }

    @GetMapping("/history/{symbol}")
    private ResponseEntity<List<CoinPriceResponseDTO>> getHistoricalPriceData(
            @PathVariable String symbol,
            @RequestParam LocalDate start,
            @RequestParam LocalDate end,
            @RequestParam(required = false, defaultValue = "usd") @ValidCurrency String currency
    ) {
        LOGGER.info("Fetching historical price data for symbol: {}, start: {}, end: {}, currency: {}", symbol, start, end, currency);
        List<CoinPriceResponseDTO> pricesList = cryptoService.getHistoricalPriceData(symbol, start, end, currency);
        LOGGER.info("Successfully fetched {} historical prices", pricesList.size());
        return ResponseEntity.ok(pricesList);
    }

    @GetMapping("/market/{symbol}")
    private ResponseEntity<CoinMarketDataResponseDTO> getCryptocurrencyMarketData(
            @PathVariable String symbol,
            @RequestParam (required = false, defaultValue = "usd") @ValidCurrency String currency
    ) {
        LOGGER.info("Fetching market data for symbol: {} and currency: {}", symbol, currency);
        CoinMarketDataResponseDTO marketData = cryptoService.getCryptocurrencyMarketData(symbol, currency);
        LOGGER.info("Successfully fetched market data: {}", marketData);
        return ResponseEntity.ok(marketData);
    }
}
