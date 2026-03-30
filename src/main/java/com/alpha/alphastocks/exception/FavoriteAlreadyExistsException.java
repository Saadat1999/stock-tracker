package com.alpha.alphastocks.exception;

public class FavoriteAlreadyExistsException extends RuntimeException{
    public FavoriteAlreadyExistsException(String message) {
        super(message);
    }
}
