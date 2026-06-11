package com.rikkei.bank.service;

import com.rikkei.bank.dto.ChangePinRequest;
import com.rikkei.bank.dto.CreateAccountRequest;
import com.rikkei.bank.dto.AccountResponse;
import com.rikkei.bank.dto.ApiResponse;

import java.util.List;

public interface AccountService {
    ApiResponse<AccountResponse> createAccount(CreateAccountRequest request);
    ApiResponse<AccountResponse> getAccountBalance(String accountNumber);
    ApiResponse<List<AccountResponse>> getMyAccounts();
    void changePin(String accountNumber, ChangePinRequest request);
}