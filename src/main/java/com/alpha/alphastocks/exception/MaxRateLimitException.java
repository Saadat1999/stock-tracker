package com.alpha.alphastocks.exception;

public class MaxRateLimitException extends RuntimeException {

    public MaxRateLimitException(String message) {
        super(message);
    }
}
