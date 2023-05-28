package com.demo.bank.domains.transaction;

public enum TransactionType {
	DEPOSIT("DEPOSIT"),
	WITHDRAW("WITHDRAW");

	private final String value;

	TransactionType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
