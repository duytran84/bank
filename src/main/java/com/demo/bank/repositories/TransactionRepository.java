package com.demo.bank.repositories;

import com.demo.bank.domains.transaction.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
	@Query("SELECT t FROM Transaction t WHERE t.accountNumber = :account_number AND t.transactionTime >= :from_time AND t.transactionTime <= :to_time")
	List<Transaction> findTransactionByAccountNumber(@Param("account_number") String accountNumber,
													 @Param("from_time") ZonedDateTime fromTime,
													 @Param("to_time") ZonedDateTime toTime);
}
