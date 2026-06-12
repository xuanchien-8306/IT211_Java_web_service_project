package com.rikkei.bank.repository;

import com.rikkei.bank.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE t.fromAccount.accountNumber = :accNo OR t.toAccount.accountNumber = :accNo ORDER BY t.timestamp DESC")
    Page<Transaction> findTransactionHistory(@Param("accNo") String accountNumber, Pageable pageable);
}