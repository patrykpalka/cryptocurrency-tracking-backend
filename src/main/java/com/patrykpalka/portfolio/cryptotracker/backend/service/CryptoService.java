package com.patrykpalka.portfolio.cryptotracker.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.patrykpalka.portfolio.cryptotracker.backend.dto.*;
import com.patrykpalka.portfolio.cryptotracker.backend.exception.CryptocurrencyDataInvalidOrMalformedException;
import com.patrykpalka.portfolio.cryptotracker.backend.exception.CryptocurrencyDataNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.patrykpalka.portfolio.cryptotracker.backend.util.CryptoUtil.*;

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

    public List<CoinPriceResponseDTO> getHistoricalPriceData(String symbol, LocalDate startDate, LocalDate endDate, String currency) {
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
            throw new RuntimeException("Received null or empty response from API for symbol: " + symbol);
        }

        LOGGER.debug("API response: {}", apiResponse);

        List<List<BigDecimal>> dateAndPriceList = apiResponse.prices();

        // If days were added before, take only days specified in parameters
        if (dateAndPriceList.size() > daysDifference + 1) {
            dateAndPriceList = dateAndPriceList.subList(0, daysDifference + 1);
            LOGGER.warn("Reverting end date. Final end date: {}",
                    convertEpochMillisecondsToLocalDate(dateAndPriceList.get(dateAndPriceList.size() - 1).get(0).longValue()));
        }

        List<CoinPriceResponseDTO> responseList = new ArrayList<>();

        for (List<BigDecimal> dateAndPrice : dateAndPriceList) {
            LocalDate date = convertEpochMillisecondsToLocalDate(dateAndPrice.get(0).longValue());
            float roundedPrice = dateAndPrice.get(1).setScale(2, RoundingMode.HALF_EVEN).floatValue();
            responseList.add(new CoinPriceResponseDTO(date, roundedPrice, currency.toUpperCase()));
        }

        return responseList;
    }

    public CoinMarketDataResponseDTO getCryptocurrencyMarketData(String id, String currency) {
        String urlPath = "/coins/" + id + "?tickers=false&community_data=false&developer_data=false";

        LOGGER.debug("Calling external API: {}", urlPath);

        JsonNode apiResponse = cryptoApiClient.get()
                .uri(urlPath)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (apiResponse == null || apiResponse.get("symbol") == null || apiResponse.get("market_data") == null) {
            throw new CryptocurrencyDataInvalidOrMalformedException("Invalid or incomplete market data for: " + id);
        }

        JsonNode marketDataNode = apiResponse.get("market_data");
        if (marketDataNode.get("market_cap") == null ||
                marketDataNode.get("total_volume") == null ||
                marketDataNode.get("circulating_supply") == null) {
            throw new CryptocurrencyDataNotFoundException("Market data not found for: " + id);
        }

        return new CoinMarketDataResponseDTO(
                id,
                apiResponse.get("symbol").asText().toUpperCase(),
                marketDataNode.get("market_cap").get(currency.toLowerCase()).asLong(),
                marketDataNode.get("total_volume").get(currency.toLowerCase()).asLong(),
                marketDataNode.get("circulating_supply").asLong(),
                currency.toUpperCase()
        );
    }
}
