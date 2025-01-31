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
        return buildErrorResponse(e, HttpStatus.SERVICE_UNAVAILABLE, request, "WebClientException");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDTO> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        return buildErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR, request, "RuntimeException");
    }

    private ResponseEntity<ErrorResponseDTO> buildErrorResponse(Exception e, HttpStatus status, HttpServletRequest request, String errorType) {
        String message = e.getMessage();
        String timestamp = LocalDateTime.now().toString();
        String path = request.getRequestURI();

        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(message, timestamp, path);

        LOGGER.error("{}: {} - Path: {}", errorType, message, path, e);

        return ResponseEntity.status(status).body(errorResponseDTO);
    }
}
