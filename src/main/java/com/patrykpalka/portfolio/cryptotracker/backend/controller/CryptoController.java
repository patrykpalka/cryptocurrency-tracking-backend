package com.patrykpalka.portfolio.cryptotracker.backend.controller;

import com.patrykpalka.portfolio.cryptotracker.backend.dto.CryptoPricesResponseDTO;
import com.patrykpalka.portfolio.cryptotracker.backend.service.CryptoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/crypto")
public class CryptoController {

    private final CryptoService cryptoService;

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
}
