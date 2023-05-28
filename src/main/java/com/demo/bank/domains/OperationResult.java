package com.demo.bank.domains;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Optional;

@Getter
@Builder
public class OperationResult<T> {
	private OperationStatus status;
	private T entity;
	private List<String> messages;

	public Optional<T> getEntity() {
		return Optional.ofNullable(entity);
	}

	public String peekMessage() {
		if (messages == null || messages.size() == 0) {
			return "No message found.";
		}

		return messages.get(0);
	}

	public enum OperationStatus {
		SUCCESS,
		FAILED
	}
}
