package com.rikkei.bank.exception;

public class InvalidPinException extends BusinessException {
    public InvalidPinException(String message) {
        super(message);
    }
}
