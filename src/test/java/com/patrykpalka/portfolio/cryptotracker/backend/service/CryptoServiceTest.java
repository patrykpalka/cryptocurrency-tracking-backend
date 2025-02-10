package com.patrykpalka.portfolio.cryptotracker.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.patrykpalka.portfolio.cryptotracker.backend.dto.CoinMarketDataResponseDTO;
import com.patrykpalka.portfolio.cryptotracker.backend.dto.CoinPriceResponseApiDTO;
import com.patrykpalka.portfolio.cryptotracker.backend.dto.CoinPriceResponseDTO;
import com.patrykpalka.portfolio.cryptotracker.backend.exception.CryptocurrencyDataInvalidOrMalformedException;
import com.patrykpalka.portfolio.cryptotracker.backend.exception.CryptocurrencyDataNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CryptoServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private CryptoService cryptoService;

    @BeforeEach
    void setUp() {
        cryptoService = new CryptoService(webClient);

        // Set up WebClient mock chain
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void shouldReturnHistoricalPriceDataForLessThan90Days() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 3, 1);
        String currency = "USD";
        String symbol = "bitcoin";

        List<List<BigDecimal>> prices = Arrays.asList(
                Arrays.asList(BigDecimal.valueOf(1704067200000L), BigDecimal.valueOf(45000.00)), // 2024-01-01
                Arrays.asList(BigDecimal.valueOf(1704153600000L), BigDecimal.valueOf(45500.00))  // 2024-01-02
        );

        CoinPriceResponseApiDTO apiResponse = new CoinPriceResponseApiDTO(prices, null, null);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(apiResponse));

        // When
        List<CoinPriceResponseDTO> result = cryptoService.getHistoricalPriceData(symbol, startDate, endDate, currency);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(LocalDate.of(2024, 1, 1), result.get(0).date());
        assertEquals(45000.00f, result.get(0).price());
        assertEquals("USD", result.get(0).currency());
    }

    @Test
    void shouldAdjustEndDateForPeriodLessThan90Days() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 2, 1);  // 31 days period
        String currency = "usd";
        String symbol = "bitcoin";

        List<List<BigDecimal>> prices = Arrays.asList(
                Arrays.asList(BigDecimal.valueOf(1704067200000L), BigDecimal.valueOf(45000.00))
        );

        CoinPriceResponseApiDTO apiResponse = new CoinPriceResponseApiDTO(prices, null, null);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(apiResponse));

        // When
        cryptoService.getHistoricalPriceData(symbol, startDate, endDate, currency);

        // Then
        // Verify that URI contains the adjusted end date (start date + 91 days)
        LocalDate expectedAdjustedDate = startDate.plusDays(91);
        long expectedUnixTimestamp = expectedAdjustedDate.atStartOfDay(ZoneId.of("UTC"))
                .toInstant()
                .getEpochSecond();

        verify(requestHeadersUriSpec).uri(String.format(
                "/coins/%s/market_chart/range?vs_currency=%s&from=%d&to=%d",
                symbol,
                currency,
                startDate.atStartOfDay(ZoneId.of("UTC")).toInstant().getEpochSecond(),
                expectedUnixTimestamp
        ));
    }

    @Test
    void shouldHandleNullApiResponse() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 3, 1);
        String currency = "USD";
        String symbol = "bitcoin";

        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.empty());

        // When & Then
        assertThrows(RuntimeException.class, () ->
                cryptoService.getHistoricalPriceData(symbol, startDate, endDate, currency));
    }

    @Test
    void shouldRoundPricesToTwoDecimalPlaces() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 3, 1);
        String currency = "EUR";
        String symbol = "ethereum";

        List<List<BigDecimal>> prices = Arrays.asList(
                Arrays.asList(BigDecimal.valueOf(1704067200000L), BigDecimal.valueOf(2500.126)), // Should round to 2500.13
                Arrays.asList(BigDecimal.valueOf(1704153600000L), BigDecimal.valueOf(2600.124))  // Should round to 2600.12
        );

        CoinPriceResponseApiDTO apiResponse = new CoinPriceResponseApiDTO(prices, null, null);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(apiResponse));

        // When
        List<CoinPriceResponseDTO> result = cryptoService.getHistoricalPriceData(symbol, startDate, endDate, currency);

        // Then
        assertEquals(2500.13f, result.get(0).price());
        assertEquals(2600.12f, result.get(1).price());
    }

    @Test
    void shouldReturnCorrectCurrencyInUpperCase() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 3, 1);
        String currency = "eur";
        String symbol = "bitcoin";

        List<List<BigDecimal>> prices = Arrays.asList(
                Arrays.asList(BigDecimal.valueOf(1704067200000L), BigDecimal.valueOf(45000.00))
        );

        CoinPriceResponseApiDTO apiResponse = new CoinPriceResponseApiDTO(prices, null, null);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(apiResponse));

        // When
        List<CoinPriceResponseDTO> result = cryptoService.getHistoricalPriceData(symbol, startDate, endDate, currency);

        // Then
        assertEquals("EUR", result.get(0).currency());
    }

    @Test
    void CryptoService_getCryptocurrencyMarketData_ReturnsResponseDTO() {
        // Given
        JsonNode mockResponse = mock(JsonNode.class);
        JsonNode marketDataNode = mock(JsonNode.class);
        JsonNode marketCapNode = mock(JsonNode.class);
        JsonNode totalVolumeNode = mock(JsonNode.class);

        when(mockResponse.get("symbol")).thenReturn(new TextNode("btc"));
        when(mockResponse.get("market_data")).thenReturn(marketDataNode);

        when(marketDataNode.get("market_cap")).thenReturn(marketCapNode);
        when(marketCapNode.get("usd")).thenReturn(new LongNode(1373546629363L));

        when(marketDataNode.get("total_volume")).thenReturn(totalVolumeNode);
        when(totalVolumeNode.get("usd")).thenReturn(new LongNode(18867210007L));

        when(marketDataNode.get("circulating_supply")).thenReturn(new LongNode(19675962L));

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(mockResponse));

        // When
        CoinMarketDataResponseDTO result = cryptoService.getCryptocurrencyMarketData("bitcoin", "usd");

        // Then
        CoinMarketDataResponseDTO expectedResponse = new CoinMarketDataResponseDTO(
                "bitcoin",
                "BTC",
                1373546629363L,
                18867210007L,
                19675962,
                "USD"
        );

        assertNotNull(result);
        assertEquals(expectedResponse, result);
    }

    @Test
    void CryptoService_getCryptocurrencyMarketData_ThrowsExceptionWhenMarketDataIsEmpty() {
        // Given
        JsonNode mockResponse = mock(JsonNode.class);
        JsonNode marketDataNode = mock(JsonNode.class);

        when(mockResponse.get("symbol")).thenReturn(new TextNode("btc"));
        when(mockResponse.get("market_data")).thenReturn(marketDataNode);
        when(marketDataNode.get("market_cap")).thenReturn(JsonNodeFactory.instance.objectNode());
        when(marketDataNode.get("total_volume")).thenReturn(JsonNodeFactory.instance.objectNode());
        when(marketDataNode.get("circulating_supply")).thenReturn(null);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(mockResponse));

        // When & Then
        assertThrows(CryptocurrencyDataNotFoundException.class,
                () -> cryptoService.getCryptocurrencyMarketData("bitcoin", "usd"));
    }

    @Test
    void CryptoService_getCryptocurrencyMarketData_ExternalAPIFailure() {
        // Given
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenThrow(WebClientResponseException.class);

        // When & Then
        assertThrows(WebClientResponseException.class,
                () -> cryptoService.getCryptocurrencyMarketData("bitcoin", "usd"));
    }

    @Test
    void CryptoService_getCryptocurrencyMarketData_ThrowsExceptionWhenMarketDataIsInvalidOrMalformed() {
        // Given
        JsonNode mockResponse = mock(JsonNode.class);

        when(mockResponse.get("id")).thenReturn(new TextNode("bitcoin"));
        when(mockResponse.get("symbol")).thenReturn(new TextNode("btc"));
        when(mockResponse.get("market_data")).thenReturn(JsonNodeFactory.instance.objectNode());

        // When & Then
        assertThrows(CryptocurrencyDataInvalidOrMalformedException.class,
                () -> cryptoService.getCryptocurrencyMarketData("bitcoin", "usd"));
    }
}
