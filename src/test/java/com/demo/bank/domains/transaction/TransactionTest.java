package com.demo.bank.domains.transaction;

import com.demo.bank.domains.OperationResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.demo.bank.domains.OperationResult.OperationStatus.FAILED;
import static com.demo.bank.domains.OperationResult.OperationStatus.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TransactionTest {

	@Test
	public void validate_zeroAmount_isValid() {
		Transaction transaction = new Transaction();
		transaction.initialize();
		transaction.setAccountNumber("1234");
		transaction.setAmount(BigDecimal.ZERO);
		transaction.setTransactionType(TransactionType.DEPOSIT);

		assertEquals(SUCCESS, transaction.validate().getStatus());
	}

	@Test
	public void validate_negativeAmount_shouldFail() {
		Transaction transaction = new Transaction();
		transaction.initialize();
		transaction.setAccountNumber("1234");
		transaction.setAmount(BigDecimal.valueOf(-1));

		OperationResult<Transaction> operationResult = transaction.validate();
		assertEquals(FAILED, operationResult.getStatus());
		assertEquals("Transact amount can not be less than 0.", operationResult.peekMessage());
	}

	@Test
	public void validate_noAccountNumber_shouldFail() {
		Transaction transaction = new Transaction();
		transaction.initialize();
		transaction.setAccountNumber("");

		OperationResult<Transaction> operationResult = transaction.validate();
		assertEquals(FAILED, operationResult.getStatus());
		assertEquals("Account Number is required.", operationResult.peekMessage());
	}

	@Test
	public void validate_noTransactionType_shouldFail() {
		Transaction transaction = new Transaction();
		transaction.initialize();
		transaction.setAccountNumber("1111");
		transaction.setAmount(BigDecimal.TEN);

		OperationResult<Transaction> operationResult = transaction.validate();
		assertEquals(FAILED, operationResult.getStatus());
		assertEquals("Transaction type is required.", operationResult.peekMessage());
	}

	@Test
	public void markExecuted_happyPath() {
		Transaction transaction = new Transaction();
		transaction.initialize();
		transaction.setAccountNumber("1111");
		transaction.setAmount(BigDecimal.TEN);
		transaction.setTransactionType(TransactionType.DEPOSIT);

		transaction.markExecuted();
		assertEquals(TransactionStatus.EXECUTED, transaction.getTransactionStatus());
		assertNotNull(transaction.getTransactionTime());
	}

	@Test
	public void markRejected_happyPath() {
		Transaction transaction = new Transaction();
		transaction.initialize();
		transaction.setAccountNumber("1111");
		transaction.setAmount(BigDecimal.TEN);
		transaction.setTransactionType(TransactionType.DEPOSIT);

		transaction.markRejected();
		assertEquals(TransactionStatus.REJECTED, transaction.getTransactionStatus());
		assertNotNull(transaction.getTransactionTime());
	}
}
