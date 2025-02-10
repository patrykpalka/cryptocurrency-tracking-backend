package com.patrykpalka.portfolio.cryptotracker.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CoinMarketDataResponseDTO(
        String id,

        String symbol,

        @JsonProperty("market_cap")
        long marketCap,

        @JsonProperty("24h_volume")
        long volume24h,

        @JsonProperty("circulating_supply")
        long circulatingSupply,

        String currency
) {
}
