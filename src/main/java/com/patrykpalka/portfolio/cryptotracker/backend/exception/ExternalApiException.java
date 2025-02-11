package com.patrykpalka.portfolio.cryptotracker.backend.exception;

public class ExternalApiException extends RuntimeException {
    public ExternalApiException(String message) {
        super(message);
    }
}
