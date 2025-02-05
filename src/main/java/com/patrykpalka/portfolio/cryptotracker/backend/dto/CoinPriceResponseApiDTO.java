package com.patrykpalka.portfolio.cryptotracker.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record CoinPriceResponseApiDTO (
        List<List<BigDecimal>> prices,
        List<List<Double>> market_caps,
        List<List<Double>> total_volumes
) {
}
