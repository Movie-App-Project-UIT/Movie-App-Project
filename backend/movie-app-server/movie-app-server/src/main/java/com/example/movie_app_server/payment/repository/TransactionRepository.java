package com.example.movie_app_server.payment.repository;

import com.example.movie_app_server.payment.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTxnRef(String txnRef);
}
