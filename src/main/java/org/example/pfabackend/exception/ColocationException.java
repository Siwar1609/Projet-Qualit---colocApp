package org.example.pfabackend.exception;

public class ColocationException extends RuntimeException {
    public ColocationException(String message) {
        super(message);
    }

    public ColocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
