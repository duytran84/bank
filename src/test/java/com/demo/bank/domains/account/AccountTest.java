package com.demo.bank.domains.account;

import com.demo.bank.domains.OperationResult;
import com.demo.bank.domains.transaction.Transaction;
import com.demo.bank.domains.transaction.TransactionStatus;
import com.demo.bank.domains.transaction.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.demo.bank.domains.OperationResult.OperationStatus.FAILED;
import static com.demo.bank.domains.OperationResult.OperationStatus.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AccountTest {

	@Test
	public void validate_zeroBalance_isValid() {
		Account account = new Account();
		account.initialize();
		account.setAccountNumber("1234");
		account.setBalance(BigDecimal.ZERO);

		assertEquals(SUCCESS, account.validate().getStatus());
	}

	@Test
	public void validate_negativeBalance_shouldFail() {
		Account account = new Account();
		account.initialize();
		account.setAccountNumber("1234");
		account.setBalance(BigDecimal.valueOf(-1));

		OperationResult<Account> operationResult = account.validate();
		assertEquals(FAILED, operationResult.getStatus());
		assertEquals("Balance can not be less than 0.", operationResult.peekMessage());
	}

	@Test
	public void validate_noAccountNumber_shouldFail() {
		Account account = new Account();
		account.initialize();
		account.setBalance(BigDecimal.valueOf(100));
		account.setAccountNumber("");

		OperationResult<Account> operationResult = account.validate();
		assertEquals(FAILED, operationResult.getStatus());
		assertEquals("Account Number is required.", operationResult.peekMessage());
	}

	@Test
	public void executeTransaction_happyPath() {
		String accountNumber = "12345";
		Account account = new Account();
		account.initialize();
		account.setAccountNumber(accountNumber);
		account.setAccountType(AccountType.TRANSACTIONAL);
		account.setBalance(BigDecimal.valueOf(1000.0075));

		Transaction transaction1 = createTransaction(BigDecimal.valueOf(400), TransactionType.WITHDRAW, accountNumber);
		OperationResult<Account> operationResult = account.executeTransaction(transaction1);
		assertEquals(SUCCESS, operationResult.getStatus());
		assertEquals(BigDecimal.valueOf(600.0075), account.getBalance());
		assertEquals(TransactionStatus.EXECUTED, transaction1.getTransactionStatus());

		Transaction transaction2 = createTransaction(BigDecimal.valueOf(600), TransactionType.WITHDRAW, accountNumber);
		operationResult = account.executeTransaction(transaction2);
		assertEquals(SUCCESS, operationResult.getStatus());
		assertEquals(BigDecimal.valueOf(0.0075), account.getBalance());
		assertEquals(TransactionStatus.EXECUTED, transaction2.getTransactionStatus());

		Transaction transaction3 = createTransaction(BigDecimal.valueOf(1), TransactionType.WITHDRAW, accountNumber);
		operationResult = account.executeTransaction(transaction3);
		assertEquals(FAILED, operationResult.getStatus());
		assertEquals(BigDecimal.valueOf(0.0075), account.getBalance());
		assertEquals(TransactionStatus.REJECTED, transaction3.getTransactionStatus());

		Transaction transaction4 = createTransaction(BigDecimal.valueOf(100), TransactionType.DEPOSIT, accountNumber);
		operationResult = account.executeTransaction(transaction4);
		assertEquals(SUCCESS, operationResult.getStatus());
		assertEquals(BigDecimal.valueOf(100.0075), account.getBalance());
		assertEquals(TransactionStatus.EXECUTED, transaction4.getTransactionStatus());

		Transaction transaction5 = createTransaction(BigDecimal.valueOf(1000), TransactionType.WITHDRAW, accountNumber);
		operationResult = account.executeTransaction(transaction5);
		assertEquals(FAILED, operationResult.getStatus());
		assertEquals(BigDecimal.valueOf(100.0075), account.getBalance());
		assertEquals(TransactionStatus.REJECTED, transaction5.getTransactionStatus());
	}

	@Test
	public void executeTransaction_wrongAccountNumber_shouldReject() {
		Account account = new Account();
		account.initialize();
		account.setAccountNumber("1111");
		account.setAccountType(AccountType.TRANSACTIONAL);
		account.setBalance(BigDecimal.valueOf(1000.0075));

		Transaction transaction1 = createTransaction(BigDecimal.valueOf(400), TransactionType.WITHDRAW, "2222");
		OperationResult<Account> operationResult = account.executeTransaction(transaction1);
		assertEquals(FAILED, operationResult.getStatus());
		assertEquals(BigDecimal.valueOf(1000.0075), account.getBalance());
		assertEquals(TransactionStatus.REJECTED, transaction1.getTransactionStatus());
	}

	private Transaction createTransaction(BigDecimal amount, TransactionType transactionType, String accountNumber) {
		Transaction transaction = new Transaction();
		transaction.initialize();
		transaction.setAmount(amount);
		transaction.setTransactionType(transactionType);
		transaction.setAccountNumber(accountNumber);
		return transaction;
	}

}
