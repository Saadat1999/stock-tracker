package com.alpha.alphastocks.exception;

public class EmptyQuoteException extends RuntimeException{
    public EmptyQuoteException(String message) {
        super(message);
    }
}
