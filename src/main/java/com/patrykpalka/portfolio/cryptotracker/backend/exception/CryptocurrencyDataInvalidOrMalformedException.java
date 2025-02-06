package com.patrykpalka.portfolio.cryptotracker.backend.exception;

public class CryptocurrencyDataInvalidOrMalformedException extends RuntimeException {
    public CryptocurrencyDataInvalidOrMalformedException(String message) {
        super(message);
    }
}
