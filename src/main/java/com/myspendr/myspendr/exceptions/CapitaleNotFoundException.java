package com.myspendr.myspendr.exceptions;

public class CapitaleNotFoundException extends RuntimeException {

    public CapitaleNotFoundException(String message) {
        super(message);
    }

    public CapitaleNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
