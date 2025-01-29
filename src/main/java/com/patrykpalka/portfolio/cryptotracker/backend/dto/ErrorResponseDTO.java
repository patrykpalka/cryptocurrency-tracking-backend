package com.patrykpalka.portfolio.cryptotracker.backend.dto;

public record ErrorResponseDTO(
        String message,
        String timestamp,
        String path
) {
}
