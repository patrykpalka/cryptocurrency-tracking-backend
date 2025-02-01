package com.patrykpalka.portfolio.cryptotracker.backend.controller;

import com.patrykpalka.portfolio.cryptotracker.backend.dto.CoinPriceResponseDTO;
import com.patrykpalka.portfolio.cryptotracker.backend.dto.CoinsListDTO;
import com.patrykpalka.portfolio.cryptotracker.backend.dto.CryptoPricesResponseDTO;
import com.patrykpalka.portfolio.cryptotracker.backend.service.CryptoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(required = false, defaultValue = "usd") String currency
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
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false, defaultValue = "usd") String currency
    ) {
        LOGGER.info("Fetching historical price data for symbol: {}, start: {}, end: {}, currency: {}", symbol, start, end, currency);
        List<CoinPriceResponseDTO> pricesList = cryptoService.getHistoricalPriceData(symbol, start, end, currency);
        LOGGER.info("Successfully fetched {} historical prices", pricesList.size());
        return ResponseEntity.ok(pricesList);
    }
}
