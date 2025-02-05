package com.patrykpalka.portfolio.cryptotracker.backend.dto;

import java.time.LocalDate;

public record CoinPriceResponseDTO(
        LocalDate date,
        float price,
        String currency
) {
}
