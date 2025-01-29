package com.patrykpalka.portfolio.cryptotracker.backend.service;

import com.patrykpalka.portfolio.cryptotracker.backend.dto.CryptoPricesResponseDTO;
import com.patrykpalka.portfolio.cryptotracker.backend.dto.MarketDataApiResponseDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CryptoService {

    private final WebClient cryptoApiClient;

    public CryptoService(WebClient cryptoApiClient) {
        this.cryptoApiClient = cryptoApiClient;
    }

    public List<CryptoPricesResponseDTO> getCryptoPrices(String symbols, String currency) {
        String urlPath = "/coins/markets?vs_currency=" + currency.toLowerCase();

        List<MarketDataApiResponseDTO> marketDataApiResponse = cryptoApiClient.get()
                .uri(urlPath + (symbols != null ? "&ids=" + symbols : ""))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<MarketDataApiResponseDTO>>() {})
                .block();

        return Optional.ofNullable(marketDataApiResponse)
                .orElseGet(Collections::emptyList)
                .stream()
                .map(dto -> new CryptoPricesResponseDTO(
                        dto.symbol().toUpperCase(),
                        dto.currentPrice(),
                        currency.toUpperCase()))
                .collect(Collectors.toList());
    }
}
