package com.demo.bank.domains.transaction;

public enum TransactionStatus {
	PENDING("PENDING"),
	EXECUTED("EXECUTED"),
	REJECTED("REJECTED");

	private final String value;

	TransactionStatus(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
