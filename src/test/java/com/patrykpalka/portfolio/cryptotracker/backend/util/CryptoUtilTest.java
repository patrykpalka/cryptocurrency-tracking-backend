package com.patrykpalka.portfolio.cryptotracker.backend.util;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class CryptoUtilTest {

    @Test
    void shouldConvertToCorrectUnixDate() {
        // Given
        LocalDate date = LocalDate.of(2025, 2, 1);
        long expectedEpochSeconds = date.atStartOfDay(ZoneId.of("UTC")).toEpochSecond();

        // When
        long actualEpochSeconds = CryptoUtil.convertToUnixDate(date);

        // Then
        assertEquals(expectedEpochSeconds, actualEpochSeconds, "Unix timestamp does not match expected value.");
    }

    @Test
    void shouldConvertEpochMillisecondsToLocalDate() {
        // Given
        LocalDate expectedDate = LocalDate.of(2024, 1, 1);
        long epochMilliseconds = expectedDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli();

        // When
        LocalDate actualDate = CryptoUtil.convertEpochMillisecondsToLocalDate(epochMilliseconds);

        // Then
        assertEquals(expectedDate, actualDate, "Converted date does not match expected value.");
    }

    @Test
    void shouldConvertEpochStartDateCorrectly() {
        // Given: Unix epoch start
        LocalDate expectedDate = LocalDate.of(1970, 1, 1);
        long epochMilliseconds = 0L;

        // When
        LocalDate actualDate = CryptoUtil.convertEpochMillisecondsToLocalDate(epochMilliseconds);

        // Then
        assertEquals(expectedDate, actualDate, "Epoch start date conversion failed.");
    }

    @Test
    void shouldConvertLeapYearDateCorrectly() {
        // Given: Leap year test
        LocalDate expectedDate = LocalDate.of(2024, 2, 29);
        long epochMilliseconds = expectedDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli();

        // When
        LocalDate actualDate = CryptoUtil.convertEpochMillisecondsToLocalDate(epochMilliseconds);

        // Then
        assertEquals(expectedDate, actualDate, "Leap year date conversion failed.");
    }

    @Test
    void shouldHandleDateInDistantPast() {
        // Given: A historical date
        LocalDate expectedDate = LocalDate.of(1900, 1, 1);
        long epochMilliseconds = expectedDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli();

        // When
        LocalDate actualDate = CryptoUtil.convertEpochMillisecondsToLocalDate(epochMilliseconds);

        // Then
        assertEquals(expectedDate, actualDate, "Distant past date conversion failed.");
    }

    @Test
    void shouldHandleDateInFarFuture() {
        // Given: A date in the far future
        LocalDate expectedDate = LocalDate.of(3000, 1, 1);
        long epochMilliseconds = expectedDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli();

        // When
        LocalDate actualDate = CryptoUtil.convertEpochMillisecondsToLocalDate(epochMilliseconds);

        // Then
        assertEquals(expectedDate, actualDate, "Far future date conversion failed.");
    }
}
