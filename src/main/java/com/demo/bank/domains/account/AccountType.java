package com.demo.bank.domains.account;

public enum AccountType {
	SAVING("SAVING"),
	TRANSACTIONAL("TRANSACTIONAL");

	private final String value;

	AccountType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
