package com.demo.bank.resources;


import com.demo.bank.domains.OperationResult;
import com.demo.bank.domains.account.Account;
import com.demo.bank.domains.transaction.Transaction;
import com.demo.bank.services.account.AccountService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(
	value = "/api/accounts",
	consumes = MediaType.APPLICATION_JSON_VALUE,
	produces = MediaType.APPLICATION_JSON_VALUE
)
public class AccountResource {
	private final AccountService accountService;
	private final OperationResultTranslator operationResultTranslator;

	public AccountResource(AccountService accountService,
						   OperationResultTranslator operationResultTranslator) {
		this.accountService = accountService;
		this.operationResultTranslator = operationResultTranslator;
	}

	@PostMapping
	public ResponseEntity<Account> createAccount(@Valid @RequestBody Account newAccount) {
		OperationResult<Account> operationResult = accountService.saveAccount(newAccount);
		return operationResultTranslator.translateResult(operationResult, HttpStatus.CREATED);
	}

	@GetMapping("/{accountNumber}")
	public ResponseEntity<Account> getAccounts(@PathVariable("accountNumber") String accountNumber) {
		OperationResult<Account> operationResult = accountService.getAccountByAccountNumber(accountNumber);
		return operationResultTranslator.translateResult(operationResult, HttpStatus.OK);
	}

	@PostMapping("/{accountNumber}/transactions")
	public ResponseEntity<String> postTransaction(@PathVariable("accountNumber") String accountNumber,
												  @Valid @RequestBody Transaction transaction) {
		transaction.setAccountNumber(accountNumber);
		return operationResultTranslator.translateResultToStringResponse(
			accountService.postTransaction(transaction), HttpStatus.CREATED);
	}

	@GetMapping("/{accountNumber}/transactions")
	public ResponseEntity<List<Transaction>> getAccounts(@PathVariable("accountNumber") String accountNumber,
														 @RequestParam("fromTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
														 @RequestParam("toTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
		ZonedDateTime fromTime = ZonedDateTime.of(fromDate, LocalTime.MIN, ZoneId.of("UTC"));
		ZonedDateTime toTime = ZonedDateTime.of(toDate, LocalTime.MAX, ZoneId.of("UTC"));
		return new ResponseEntity<>(accountService.listTransaction(accountNumber, fromTime, toTime), HttpStatus.OK);
	}

}
