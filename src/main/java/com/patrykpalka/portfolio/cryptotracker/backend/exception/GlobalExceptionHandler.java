package com.patrykpalka.portfolio.cryptotracker.backend.exception;

import com.patrykpalka.portfolio.cryptotracker.backend.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(WebClientException.class)
    public ResponseEntity<ErrorResponseDTO> handleWebClientException(WebClientException e, HttpServletRequest request) {
        String message = e.getMessage();
        String timestamp = LocalDateTime.now().toString();
        String path = request.getRequestURI();

        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(message, timestamp, path);

        LOGGER.error("WebClient Error: {} - Path: {}", message, path, e);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponseDTO);
    }
}
