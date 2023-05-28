package com.demo.bank.domains.transaction;

import com.demo.bank.domains.OperationResult;
import com.demo.bank.domains.OperationResult.OperationResultBuilder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "tbl_transactions")
public class Transaction {
	@Id
	private UUID id;

	private String accountNumber;

	@NotNull(message = "Transaction type is required")
	@Enumerated(EnumType.STRING)
	private TransactionType transactionType;

	private String referenceNo;

	@Enumerated(EnumType.STRING)
	private TransactionStatus transactionStatus;

	@PositiveOrZero(message = "Invalid transaction amount")
	private BigDecimal amount;

	private String transactionNote;

	private ZonedDateTime transactionTime;

	private ZonedDateTime createdTime;

	public void initialize() {
		setId(UUID.randomUUID());
		setCreatedTime(ZonedDateTime.now());
		setTransactionStatus(TransactionStatus.PENDING);
		setReferenceNo(UUID.randomUUID().toString());
	}

	public OperationResult<Transaction> validate() {
		OperationResultBuilder<Transaction> resultBuilder = OperationResult.builder();
		resultBuilder.status(OperationResult.OperationStatus.SUCCESS);
		resultBuilder.entity(this);
		List<String> errorMessages = new ArrayList<>();

		if (accountNumber == null || accountNumber.isBlank()) {
			resultBuilder.status(OperationResult.OperationStatus.FAILED);
			errorMessages.add("Account Number is required.");
		}

		if (referenceNo == null || referenceNo.isBlank()) {
			resultBuilder.status(OperationResult.OperationStatus.FAILED);
			errorMessages.add("Reference Number is required.");
		}

		if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
			resultBuilder.status(OperationResult.OperationStatus.FAILED);
			errorMessages.add("Transact amount can not be less than 0.");
		}

		if (transactionType == null) {
			resultBuilder.status(OperationResult.OperationStatus.FAILED);
			errorMessages.add("Transaction type is required.");
		}

		resultBuilder.messages(errorMessages);
		return resultBuilder.build();
	}

	public void markExecuted() {
		this.setTransactionTime(ZonedDateTime.now());
		this.setTransactionStatus(TransactionStatus.EXECUTED);
	}

	public void markRejected() {
		this.setTransactionTime(ZonedDateTime.now());
		this.setTransactionStatus(TransactionStatus.REJECTED);
	}
}
