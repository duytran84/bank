package com.demo.bank.services;

import com.demo.bank.domains.OperationResult;
import com.demo.bank.domains.account.Account;
import com.demo.bank.domains.transaction.Transaction;
import com.demo.bank.domains.transaction.TransactionStatus;
import com.demo.bank.domains.transaction.TransactionType;
import com.demo.bank.repositories.AccountRepository;
import com.demo.bank.repositories.TransactionRepository;
import com.demo.bank.services.account.AccountService;
import com.demo.bank.services.account.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static com.demo.bank.domains.OperationResult.OperationStatus.FAILED;
import static com.demo.bank.domains.OperationResult.OperationStatus.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
	@Mock
	private AccountRepository accountRepository;
	@Mock
	private TransactionRepository transactionRepository;

	private AccountService accountService;
	private String testAccountNumber = "12345";
	private Account testAccount;
	private Transaction testTransaction;

	@BeforeEach
	public void setUp() {
		testAccount = new Account();
		testAccount.initialize();
		testAccount.setAccountNumber(testAccountNumber);
		testAccount.setBalance(BigDecimal.TEN);

		testTransaction = new Transaction();
		testTransaction.initialize();
		testTransaction.setAccountNumber(testAccountNumber);
		testTransaction.setTransactionType(TransactionType.DEPOSIT);
		testTransaction.setAmount(BigDecimal.ONE);

		lenient().when(accountRepository.getByAccountNumber(testAccountNumber)).thenReturn(Optional.of(testAccount));
		lenient().when(accountRepository.save(testAccount)).thenReturn(testAccount);
		lenient().when(transactionRepository.findTransactionByAccountNumber(eq(testAccountNumber), any(ZonedDateTime.class), any(ZonedDateTime.class)))
			.thenReturn(List.of(testTransaction));


		this.accountService = new AccountServiceImpl(accountRepository, transactionRepository);
	}

	@Test
	public void getAccountByAccountNumber_happyPath() {
		OperationResult<Account> operationResult = accountService.getAccountByAccountNumber(testAccountNumber);
		verify(accountRepository, times(1)).getByAccountNumber(testAccountNumber);

		assertEquals(SUCCESS, operationResult.getStatus());
		assertTrue(operationResult.getEntity().isPresent());
		assertEquals(testAccountNumber, operationResult.getEntity().get().getAccountNumber());
	}

	@Test
	public void getAccountByAccountNumber_notFound() {
		String anotherAccountNumber = "9999";
		when(accountRepository.getByAccountNumber(anotherAccountNumber)).thenReturn(Optional.empty());

		OperationResult<Account> operationResult = accountService.getAccountByAccountNumber(anotherAccountNumber);
		verify(accountRepository, times(1)).getByAccountNumber(anotherAccountNumber);

		assertEquals(FAILED, operationResult.getStatus());
	}

	@Test
	public void saveAccount_happyPath() {
		OperationResult<Account> operationResult = accountService.saveAccount(testAccount);
		verify(accountRepository, times(1)).save(testAccount);

		assertEquals(SUCCESS, operationResult.getStatus());
		assertTrue(operationResult.getEntity().isPresent());
		assertEquals(testAccountNumber, operationResult.getEntity().get().getAccountNumber());
	}

	@Test
	public void saveAccount_notValid_shouldFail() {
		Account anotherAccount = new Account();
		anotherAccount.initialize();
		anotherAccount.setBalance(BigDecimal.TEN);

		OperationResult<Account> operationResult = accountService.saveAccount(anotherAccount);
		verify(accountRepository, times(0)).save(anotherAccount);

		assertEquals(FAILED, operationResult.getStatus());
	}

	@Test
	public void listTransaction_happyPath() {
		ZonedDateTime to = ZonedDateTime.now();
		ZonedDateTime from = to.minusDays(7);

		List<Transaction> transactions = accountService.listTransaction(testAccountNumber, from, to);
		verify(transactionRepository, times(1)).findTransactionByAccountNumber(testAccountNumber, from, to);

		assertEquals(1, transactions.size());
		assertEquals(testTransaction.getReferenceNo(), transactions.get(0).getReferenceNo());
	}

	@Test
	public void listTransaction_noTransaction() {
		String anotherAccountNumber = "5555";
		ZonedDateTime to = ZonedDateTime.now();
		ZonedDateTime from = to.minusDays(7);

		List<Transaction> transactions = accountService.listTransaction(anotherAccountNumber, from, to);
		verify(transactionRepository, times(1)).findTransactionByAccountNumber(anotherAccountNumber, from, to);

		assertEquals(0, transactions.size());
	}

	@Test
	public void postTransaction_happyPath() {
		OperationResult<Transaction> operationResult = accountService.postTransaction(testTransaction);
		verify(accountRepository, times(1)).getByAccountNumber(testAccountNumber);
		verify(transactionRepository, times(1)).save(testTransaction);

		assertEquals(SUCCESS, operationResult.getStatus());
		assertEquals(BigDecimal.valueOf(11), testAccount.getBalance());
		assertEquals(TransactionStatus.EXECUTED, testTransaction.getTransactionStatus());
	}

	@Test
	public void postTransaction_notFoundAccount_shouldReject() {
		Transaction anotherTransaction = new Transaction();
		anotherTransaction.initialize();
		anotherTransaction.setAccountNumber("no exist account number");
		anotherTransaction.setTransactionType(TransactionType.DEPOSIT);
		anotherTransaction.setAmount(BigDecimal.ONE);

		OperationResult<Transaction> operationResult = accountService.postTransaction(anotherTransaction);
		verify(accountRepository, times(1)).getByAccountNumber(anotherTransaction.getAccountNumber());
		verify(transactionRepository, times(0)).save(testTransaction);

		assertEquals(FAILED, operationResult.getStatus());
		assertEquals(TransactionStatus.REJECTED, anotherTransaction.getTransactionStatus());
	}

	@Test
	public void postTransaction_withdrawMoreThanBalance_shouldReject() {
		Transaction anotherTransaction = new Transaction();
		anotherTransaction.initialize();
		anotherTransaction.setAccountNumber(testAccountNumber);
		anotherTransaction.setTransactionType(TransactionType.WITHDRAW);
		anotherTransaction.setAmount(BigDecimal.valueOf(100));
		when(accountRepository.getByAccountNumber(testAccountNumber)).thenReturn(Optional.of(testAccount));

		OperationResult<Transaction> operationResult = accountService.postTransaction(anotherTransaction);
		verify(accountRepository, times(1)).getByAccountNumber(testAccountNumber);
		verify(transactionRepository, times(0)).save(testTransaction);

		assertEquals(FAILED, operationResult.getStatus());
		assertEquals(TransactionStatus.REJECTED, anotherTransaction.getTransactionStatus());
	}

}
