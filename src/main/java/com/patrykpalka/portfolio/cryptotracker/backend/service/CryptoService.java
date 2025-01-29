package com.patrykpalka.portfolio.cryptotracker.backend.service;

import com.patrykpalka.portfolio.cryptotracker.backend.dto.MarketDataApiResponseDTO;
import com.patrykpalka.portfolio.cryptotracker.backend.dto.CryptoPricesResponseDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CryptoService {

    private final WebClient cryptoApiClient;

    public CryptoService(WebClient cryptoApiClient) {
        this.cryptoApiClient = cryptoApiClient;
    }

    public List<CryptoPricesResponseDTO> getCryptoPrices(String symbols, String currency) {
        String currencyLowerCase = currency.toLowerCase();
        String urlPath = "/coins/markets?vs_currency=" + currencyLowerCase;

        List<MarketDataApiResponseDTO> marketDataApiRespons = cryptoApiClient.get()
                .uri(urlPath + (symbols != null ? "&ids=" + symbols : ""))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<MarketDataApiResponseDTO>>() {})
                .block();

        if (marketDataApiRespons == null) {
            return List.of();
        }

        return marketDataApiRespons.stream()
                .map(dto -> new CryptoPricesResponseDTO(
                        dto.symbol().toUpperCase(),
                        dto.currentPrice(),
                        currency.toUpperCase()))
                .collect(Collectors.toList());
    }
}
