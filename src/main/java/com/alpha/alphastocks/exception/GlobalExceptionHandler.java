package com.alpha.alphastocks.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FavoriteAlreadyExistsException.class)
    public ResponseEntity<String> handleFavoriteExistsException(FavoriteAlreadyExistsException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
    }

    @ExceptionHandler(MaxRateLimitException.class)
    public ResponseEntity<String> handleMaxRateLimitException(MaxRateLimitException exception) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(exception.getMessage());
    }

    @ExceptionHandler(EmptyQuoteException.class)
    public ResponseEntity<String> handleEmptyQuoteException(EmptyQuoteException exception) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(exception.getMessage());
    }

    @ExceptionHandler(NoAlphaResponseException.class)
    public ResponseEntity<String> handleNoAlphaResponseException(NoAlphaResponseException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(exception.getMessage());
    }
}
