package com.demo.bank.services.account;

import com.demo.bank.domains.account.Account;
import com.demo.bank.domains.OperationResult;
import com.demo.bank.domains.transaction.Transaction;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Service to manage accounts
 */
public interface AccountService {
	/**
	 * Save an account
	 *
	 * @param account - the account to save
	 * @return the persisted {@link Account}
	 */
	OperationResult<Account> saveAccount(Account account);

	/**
	 * Retrieve an account by account number
	 *
	 * @param accountNumber - account number of the account to retrieve
	 * @return the account if found, otherwise empty
	 */
	OperationResult<Account> getAccountByAccountNumber(String accountNumber);

	/**
	 * List transactions for an account by account number by transaction time
	 *
	 * @param accountNumber - account number of the account to list transactions
	 * @param fromTime      - list transactions from date
	 * @param toTime        - list transactions to date
	 * @return - a collection of found transactions
	 */
	List<Transaction> listTransaction(String accountNumber, ZonedDateTime fromTime, ZonedDateTime toTime);

	/**
	 * Post a transaction
	 *
	 * @param transaction - transaction to process
	 * @return - operation result
	 */
	OperationResult<Transaction> postTransaction(Transaction transaction);
}
