package com.abcode.inventoryservice.exception;

public class CustomBadRequestException extends RuntimeException {
    public CustomBadRequestException(String message) {
        super(message);
    }

    public CustomBadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomBadRequestException(Throwable cause) {
        super(cause);
    }
}
