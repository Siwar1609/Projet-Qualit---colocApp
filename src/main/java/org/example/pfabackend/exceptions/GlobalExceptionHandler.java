package org.example.pfabackend.exceptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler extends RuntimeException {

    // Handle HttpClientErrorException (e.g., 403 Forbidden, 409 Conflict)
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Map<String, Object>> handleHttpClientErrorException(HttpClientErrorException ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());

        // Use HttpStatus to get the status code and reason phrase
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase()); // Get the reason phrase
        body.put("message", extractErrorMessage(ex));
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, status);
    }

    // Helper method to extract error message from the response body
    private String extractErrorMessage(HttpClientErrorException ex) {
        try {
            // Parse the response body as JSON (if available)
            return ex.getResponseBodyAsString();
        } catch (Exception e) {
            // Fallback to the default reason phrase if parsing fails
            return ex.getMessage();
        }
    }

    // Handle generic exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());

        // Use HttpStatus.INTERNAL_SERVER_ERROR for generic exceptions
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", "An unexpected error occurred: " + ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, status);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errorDetails = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            errorDetails.put("field", ((FieldError) error).getField());
            errorDetails.put("message", error.getDefaultMessage());
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDetails);
    }
}