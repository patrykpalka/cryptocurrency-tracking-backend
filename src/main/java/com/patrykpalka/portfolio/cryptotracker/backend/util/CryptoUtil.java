package com.patrykpalka.portfolio.cryptotracker.backend.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class CryptoUtil {

    public static long convertToUnixDate(LocalDate date) {
        return date.atStartOfDay()
                .atZone(ZoneId.of("UTC"))
                .toInstant()
                .getEpochSecond();
    }

    public static LocalDate convertEpochMillisecondsToLocalDate(long epochMilliseconds) {
        return Instant.ofEpochMilli(epochMilliseconds)
                .atZone(ZoneId.of("UTC"))
                .toLocalDate();
    }
}
