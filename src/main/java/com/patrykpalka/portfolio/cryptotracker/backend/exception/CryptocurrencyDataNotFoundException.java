package com.patrykpalka.portfolio.cryptotracker.backend.exception;

public class CryptocurrencyDataNotFoundException extends RuntimeException {
    public CryptocurrencyDataNotFoundException(String message) {
        super(message);
    }
}
