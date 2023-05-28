package com.demo.bank.resources;

import com.demo.bank.domains.OperationResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class OperationResultTranslator {

	public <T> ResponseEntity<T> translateResult(OperationResult<T> operationResult, HttpStatus successStatus) {
		switch (operationResult.getStatus()) {
			case SUCCESS:
				return operationResult.getEntity()
					.map(account -> new ResponseEntity<>(account, successStatus))
					.orElse(new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
			case FAILED:
				return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
			default:
				return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public <T> ResponseEntity<String> translateResultToStringResponse(OperationResult<T> operationResult, HttpStatus successStatus) {
		switch (operationResult.getStatus()) {
			case SUCCESS:
				return new ResponseEntity<>(operationResult.peekMessage(), successStatus);
			case FAILED:
				return new ResponseEntity<>(operationResult.peekMessage(), HttpStatus.BAD_REQUEST);
			default:
				return new ResponseEntity<>(operationResult.peekMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
