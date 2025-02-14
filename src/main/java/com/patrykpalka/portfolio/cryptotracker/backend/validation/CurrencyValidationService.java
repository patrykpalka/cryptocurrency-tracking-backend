package com.patrykpalka.portfolio.cryptotracker.backend.validation;

import com.patrykpalka.portfolio.cryptotracker.backend.exception.ExternalApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
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

    @Cacheable(value = SUPPORTED_CURRENCIES_CACHE, key = "'currencies'", unless = "#result.isEmpty()")
    public Set<String> getSupportedCurrencies() {
        Set<String> currencies = fetchSupportedCurrencies();
        LOGGER.info("Successfully fetched supported currencies. Count: {}", currencies.size());
        return currencies;
    }

    @Scheduled(cron = "0 0 */12 * * *") // Evict currencies every 12 hours to have up-to-date data
    @CacheEvict(value = SUPPORTED_CURRENCIES_CACHE, allEntries = true)
    public void evictSupportedCurrencies() {
        LOGGER.info("Evicting supported currencies cache");
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

    public boolean isValidCurrency(String currency) {
        if (currency == null) {
            return false;
        }
        return getSupportedCurrencies().contains(currency.toLowerCase());
    }
}
