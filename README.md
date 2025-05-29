# Cryptocurrency Tracking App Backend

A demo application for tracking cryptocurrency prices, built with Spring Boot.

## Table of Contents
- [Description](#description)
- [Tech Stack](#tech-stack)
- [Architecture and Features](#architecture-and-features)
- [API Examples](#api-examples)
- [Requirements and Running](#requirements-and-running)
- [Currency Validation and Exception Handling](#currency-validation-and-exception-handling)
- [Author](#author)

## Description
This project allows fetching current and historical market data for cryptocurrencies, validating supported currencies, and presenting a list of available coins.
The application exposes a REST API, uses the external CoinGecko API, and implements input parameter validation.

## Tech Stack
- Java 17
- Spring Boot
- Spring WebFlux
- Spring Validation
- Spring Cache
- SLF4J (logging)
- Maven (build tool)
- JUnit 5, Mockito (unit testing)
- CoinGecko API (external data source)

## Architecture and Features
- RESTful API – fetches prices, market data, coin list, and historical data.
- Currency validation – `@ValidCurrency` annotation and custom validator checking currency availability in CoinGecko.
- Caching – caches supported currencies with automatic refresh every 12 hours.
- Exception handling – custom domain exceptions for API errors and invalid data.
- DTOs – clear separation between presentation and API communication layers.
- Unit tests – business logic covered with JUnit and Mockito.
- Configuration via `application.properties` and the `CryptoApiProperties` class.

## API Examples

- **GET /api/crypto/prices**  
  Returns current prices for selected cryptocurrencies in the given currency.
- **GET /api/crypto/supported**  
  Returns a list of supported coins.
- **GET /api/crypto/history/{symbol}**  
  Returns historical prices for a selected cryptocurrency in a given date range.
- **GET /api/crypto/market/{symbol}**  
  Returns market data (market cap, volume, supply) for a selected cryptocurrency.

Example request:
```http
GET /api/crypto/prices?symbols=bitcoin,ethereum&currency=usd
```

## Requirements and Running

Requirements:
- Java 17+
- Maven

To run the application locally:
```bash
mvn clean spring-boot:run
```
CoinGecko API configuration is located in `src/main/resources/application.properties`.

## Currency Validation and Exception Handling

- Currency validation is performed using the `@ValidCurrency` annotation on controller parameters.
- Supported currencies are fetched and cached from CoinGecko.
- In case of an invalid currency or API error, an appropriate domain exception is returned.

## Author

[Patryk Palka - GitHub](https://github.com/patrykpalka)