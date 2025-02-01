package com.patrykpalka.portfolio.cryptotracker.backend.service;

import com.patrykpalka.portfolio.cryptotracker.backend.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CryptoService {

    private final WebClient cryptoApiClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(CryptoService.class);

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

    public List<CoinsListDTO> getCoinsList() {
        return cryptoApiClient.get()
                .uri("/coins/list")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<CoinsListDTO>>() {})
                .block();
    }

    public List<CoinPriceResponseDTO> getHistoricalPriceData(String symbol, String start, String end, String currency) {
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);

        int daysDifference = (int) ChronoUnit.DAYS.between(startDate, endDate);

        // Specifying data interval to daily is not available in Demo version of CoinGecko API
        // To get daily data we need to exceed 90 days
        if (90 >= daysDifference) {
            endDate = startDate.plusDays(91);
            LOGGER.warn("Adjusted end date to {} due to API limitations (Demo version).", endDate);
        }

        String urlPathFormat = "/coins/%s/market_chart/range?vs_currency=%s&from=%s&to=%s";
        String urlPath = String.format(urlPathFormat, symbol, currency.toLowerCase(), convertToUnixDate(startDate), convertToUnixDate(endDate));

        LOGGER.debug("Calling external API: {}", urlPath);

        CoinPriceResponseApiDTO apiResponse = cryptoApiClient.get()
                .uri(urlPath)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<CoinPriceResponseApiDTO>() {})
                .block();

        if (apiResponse == null || apiResponse.prices() == null) {
            LOGGER.error("Received null or empty response from API for symbol: {}", symbol);
            throw new RuntimeException("Received null or empty response from API");
        }

        LOGGER.debug("API response: {}", apiResponse);

        List<List<BigDecimal>> dateAndPriceList = apiResponse.prices();

        // If days were added before, take only days specified in parameters
        if (dateAndPriceList.size() > daysDifference + 1) {
            dateAndPriceList = dateAndPriceList.subList(0, daysDifference + 1);
            LOGGER.warn("Reverting end date. Final end date: {}",
                    LocalDate.ofEpochDay(convertEpochMillisecondsToEpochDays(
                            dateAndPriceList.get(dateAndPriceList.size() - 1).get(0).longValue())));
        }

        List<CoinPriceResponseDTO> responseList = new ArrayList<>();

        for (List<BigDecimal> dateAndPrice : dateAndPriceList) {
            LocalDate date = LocalDate.ofEpochDay(convertEpochMillisecondsToEpochDays(dateAndPrice.get(0).longValue()));
            float roundedPrice = dateAndPrice.get(1).setScale(2, RoundingMode.HALF_EVEN).floatValue();
            responseList.add(new CoinPriceResponseDTO(date, roundedPrice, currency.toUpperCase()));
        }

        return responseList;
    }

    private long convertToUnixDate(LocalDate date) {
        return date.atStartOfDay()
                .atZone(ZoneId.of("UTC"))
                .toInstant()
                .getEpochSecond();
    }

    public long convertEpochMillisecondsToEpochDays(long epochMilliseconds) {
        return Instant.ofEpochMilli(epochMilliseconds)
                .atZone(ZoneId.of("UTC"))
                .toLocalDate()
                .toEpochDay();
    }
}
