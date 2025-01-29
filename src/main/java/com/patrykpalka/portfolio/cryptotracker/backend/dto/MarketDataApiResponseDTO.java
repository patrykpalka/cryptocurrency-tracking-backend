package com.patrykpalka.portfolio.cryptotracker.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record MarketDataApiResponseDTO(
        String symbol,

        @JsonProperty("current_price")
        BigDecimal currentPrice
) {
}
