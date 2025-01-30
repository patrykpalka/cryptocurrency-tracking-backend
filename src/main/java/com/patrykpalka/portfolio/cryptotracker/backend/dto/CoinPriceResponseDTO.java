package com.patrykpalka.portfolio.cryptotracker.backend.dto;

import java.math.BigDecimal;

public record CoinPriceResponseDTO(
        String date,
        BigDecimal price,
        String currency
) {
}
