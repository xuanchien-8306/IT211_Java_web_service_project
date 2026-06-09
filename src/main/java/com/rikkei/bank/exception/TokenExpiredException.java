package com.rikkei.bank.exception;

public class TokenExpiredException extends BusinessException {
    public TokenExpiredException(String message) {
        super(message);
    }
}
