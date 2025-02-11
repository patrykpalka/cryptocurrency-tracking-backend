package com.patrykpalka.portfolio.cryptotracker.backend.validation;

import com.patrykpalka.portfolio.cryptotracker.backend.exception.ExternalApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CurrencyValidationService {

    private final WebClient cryptoApiClient;
    private static final String SUPPORTED_CURRENCIES_CACHE = "supportedCurrencies";
    private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyValidationService.class);

    public CurrencyValidationService(WebClient cryptoApiClient) {
        this.cryptoApiClient = cryptoApiClient;
    }

    public Set<String> getSupportedCurrencies() {
        Set<String> currencies = fetchSupportedCurrencies();
        return currencies;
    }

    private Set<String> fetchSupportedCurrencies() {
        List<String> currencies = cryptoApiClient.get()
                .uri("/simple/supported_vs_currencies")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
                .block();

        if (currencies == null) {
            throw new ExternalApiException("Received null response from currencies API");
        }

        return currencies.stream()
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }
}
