package com.patrykpalka.portfolio.cryptotracker.backend.exception;

import com.patrykpalka.portfolio.cryptotracker.backend.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.reactive.function.client.WebClientException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CryptocurrencyDataInvalidOrMalformedException.class)
    public ResponseEntity<ErrorResponseDTO> handleCryptocurrencyDataInvalidOrMalformedException(CryptocurrencyDataInvalidOrMalformedException e, HttpServletRequest request) {
        return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST, request, "CryptocurrencyDataInvalidOrMalformedException");
    }

    @ExceptionHandler(CryptocurrencyDataNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleCryptocurrencyDataNotFoundException(CryptocurrencyDataNotFoundException e, HttpServletRequest request) {
        return buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND, request, "CryptocurrencyDataNotFoundException");
    }

    @ExceptionHandler(WebClientException.class)
    public ResponseEntity<ErrorResponseDTO> handleWebClientException(WebClientException e, HttpServletRequest request) {
        return buildErrorResponse(e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE, request, "WebClientException");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        if (e.getRequiredType() == LocalDate.class) {
            String message = "Invalid date format. Please use the format yyyy-MM-dd.";
            return buildErrorResponse(message, HttpStatus.BAD_REQUEST, request, "MethodArgumentTypeMismatchException");
        }
        return buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST, request, "MethodArgumentTypeMismatchException");
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ErrorResponseDTO> handleDateTimeParseException(DateTimeParseException e, HttpServletRequest request) {
        return buildErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, request, "DateTimeParseException");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDTO> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        return buildErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, request, "RuntimeException");
    }

    private ResponseEntity<ErrorResponseDTO> buildErrorResponse(String message, HttpStatus status, HttpServletRequest request, String errorType) {
        String timestamp = LocalDateTime.now().toString();
        String path = request.getRequestURI();

        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(message, timestamp, path);

        LOGGER.error("{}: {} - Path: {}", errorType, message, path);

        return ResponseEntity.status(status).body(errorResponseDTO);
    }
}
