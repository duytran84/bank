package com.demo.bank.domains.account;

import com.demo.bank.domains.OperationResult;
import com.demo.bank.domains.OperationResult.OperationResultBuilder;
import com.demo.bank.domains.OperationResult.OperationStatus;
import com.demo.bank.domains.transaction.Transaction;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.demo.bank.domains.transaction.TransactionType.DEPOSIT;

@Getter
@Setter
@Entity
@Table(name = "tbl_accounts")
public class Account {
	@Id
	private UUID id;

	private String name;

	@NotBlank(message = "Account number is required")
	private String accountNumber;

	@NotNull(message = "Account type is required")
	@Enumerated(EnumType.STRING)
	private AccountType accountType;

	private BigDecimal balance;

	private boolean active;

	private ZonedDateTime createdTime;

	private ZonedDateTime lastUpdatedTime;

	public void initialize() {
		this.setId(UUID.randomUUID());
		this.setCreatedTime(ZonedDateTime.now());
		this.setBalance(BigDecimal.ZERO);
		this.setActive(true);
	}

	public OperationResult<Account> validate() {
		OperationResultBuilder<Account> resultBuilder = OperationResult.builder();
		resultBuilder.status(OperationStatus.SUCCESS);
		resultBuilder.entity(this);
		List<String> errorMessages = new ArrayList<>();

		if (accountNumber == null || accountNumber.isBlank()) {
			resultBuilder.status(OperationStatus.FAILED);
			errorMessages.add("Account Number is required.");
		}

		if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0) {
			resultBuilder.status(OperationStatus.FAILED);
			errorMessages.add("Balance can not be less than 0.");
		}

		resultBuilder.messages(errorMessages);
		return resultBuilder.build();
	}

	public synchronized OperationResult<Account> executeTransaction(Transaction transaction) {
		OperationResult<Transaction> transactionValidationResult = transaction.validate();
		if (OperationStatus.FAILED.equals(transactionValidationResult.getStatus())) {
			transaction.markRejected();
			return OperationResult.<Account>builder()
				.status(OperationStatus.FAILED)
				.messages(transactionValidationResult.getMessages())
				.build();
		}

		OperationResult<Account> accountValidationResult = validateTransactionOperation(transaction);
		if (OperationStatus.FAILED.equals(accountValidationResult.getStatus())) {
			transaction.markRejected();
			return accountValidationResult;
		}

		OperationResult<Account> successResult = OperationResult.<Account>builder()
			.status(OperationStatus.SUCCESS)
			.messages(List.of("Transaction executed successfully."))
			.build();

		switch (transaction.getTransactionType()) {
			case DEPOSIT:
				executeTransaction(transaction.getAmount());
				transaction.markExecuted();
				return successResult;
			case WITHDRAW:
				executeTransaction(transaction.getAmount().negate());
				transaction.markExecuted();
				return successResult;
			default:
				transaction.markRejected();
				return OperationResult.<Account>builder()
					.status(OperationStatus.FAILED)
					.messages(List.of("Transaction type does not supported."))
					.build();
		}
	}

	private void executeTransaction(BigDecimal amount) {
		this.balance = balance.add(amount);
	}

	private OperationResult<Account> validateTransactionOperation(Transaction transaction) {
		OperationResultBuilder<Account> resultBuilder = OperationResult.builder();
		resultBuilder.status(OperationStatus.SUCCESS);

		if (!isActive()) {
			resultBuilder.status(OperationStatus.FAILED);
			resultBuilder.messages(List.of("Inactive account does not support transaction."));
			return resultBuilder.build();
		}

		if (!accountNumber.equals(transaction.getAccountNumber())) {
			resultBuilder.status(OperationStatus.FAILED);
			resultBuilder.messages(List.of("Account number in transaction does not match."));
			return resultBuilder.build();
		}

		if (AccountType.SAVING.equals(accountType)) {
			resultBuilder.status(OperationStatus.FAILED);
			resultBuilder.messages(List.of("Account type does not support transaction."));
			return resultBuilder.build();
		}

		BigDecimal postTransactionBalance = DEPOSIT.equals(transaction.getTransactionType())
			? balance.add(transaction.getAmount())
			: balance.subtract(transaction.getAmount());

		if (BigDecimal.ZERO.compareTo(postTransactionBalance) > 0) {
			resultBuilder.status(OperationStatus.FAILED);
			resultBuilder.messages(List.of("Balance can not be less than 0 after transaction."));
			return resultBuilder.build();
		}

		return resultBuilder.build();
	}
}
