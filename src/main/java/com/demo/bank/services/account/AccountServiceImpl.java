package com.demo.bank.services.account;

import com.demo.bank.domains.account.Account;
import com.demo.bank.domains.transaction.Transaction;
import com.demo.bank.repositories.AccountRepository;
import com.demo.bank.domains.OperationResult;
import com.demo.bank.domains.OperationResult.OperationStatus;
import com.demo.bank.repositories.TransactionRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {
	private final AccountRepository accountRepository;
	private final TransactionRepository transactionRepository;

	public AccountServiceImpl(AccountRepository accountRepository,
							  TransactionRepository transactionRepository) {
		this.accountRepository = accountRepository;
		this.transactionRepository = transactionRepository;
	}

	@Override
	@Transactional
	public OperationResult<Account> saveAccount(Account account) {
		if (account.getId() == null) {
			account.initialize();
		}

		account.setLastUpdatedTime(ZonedDateTime.now());
		OperationResult<Account> accountOperationResult = account.validate();

		if (OperationStatus.SUCCESS.equals(accountOperationResult.getStatus())) {
			try {
				return createSuccessResult(accountRepository.save(account));
			} catch (Exception exception) {
				// TODO: log exception
				return OperationResult.<Account>builder()
					.status(OperationStatus.FAILED)
					.messages(List.of("Could not save account.", exception.getMessage()))
					.build();
			}
		} else {
			return accountOperationResult;
		}
	}

	@Override
	public OperationResult<Account> getAccountByAccountNumber(String accountNumber) {
		OperationResult.OperationResultBuilder<Account> resultBuilder = OperationResult.builder();
		return accountRepository.getByAccountNumber(accountNumber)
			.map(this::createSuccessResult)
			.orElse(resultBuilder.status(OperationStatus.FAILED).build());
	}

	@Override
	public List<Transaction> listTransaction(String accountNumber, ZonedDateTime fromTime, ZonedDateTime toTime) {
		return transactionRepository.findTransactionByAccountNumber(accountNumber, fromTime, toTime);
	}

	@Override
	@Transactional
	public OperationResult<Transaction> postTransaction(Transaction transaction) {
		transaction.initialize();

		return accountRepository.getByAccountNumber(transaction.getAccountNumber())
			.map(account -> {
				OperationResult<Account> accountOperationResult = account.executeTransaction(transaction);

				if (OperationStatus.SUCCESS.equals(accountOperationResult.getStatus())) {
					accountRepository.save(account);
				}

				transactionRepository.save(transaction);

				return OperationResult.<Transaction>builder()
					.status(accountOperationResult.getStatus())
					.messages(accountOperationResult.getMessages())
					.build();
			})
			.orElseGet(() -> {
				transaction.markRejected();
				transactionRepository.save(transaction);

				return OperationResult.<Transaction>builder()
					.status(OperationStatus.FAILED)
					.messages(List.of("Could not find account %s for transaction."))
					.build();
			});
	}

	private OperationResult<Account> createSuccessResult(Account account) {
		return OperationResult.<Account>builder()
			.status(OperationStatus.SUCCESS)
			.entity(account)
			.build();
	}
}
