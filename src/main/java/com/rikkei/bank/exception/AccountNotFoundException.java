package com.rikkei.bank.exception;

public class AccountNotFoundException extends BusinessException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
