package com.patrykpalka.portfolio.cryptotracker.backend.dto;

import java.math.BigDecimal;

public record CryptoPricesResponseDTO(
        String symbol,
        BigDecimal price,
        String currency
) {
}
