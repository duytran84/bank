package com.demo.bank.resources.handlers;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ResponseExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(value = {IllegalArgumentException.class, DataAccessException.class})
	protected ResponseEntity<Object> handleBadRequestException(RuntimeException exception, WebRequest request) {
		String bodyOfResponse = "Invalid input provided, please review input data.";
		return handleExceptionInternal(exception, bodyOfResponse, new HttpHeaders(),
			HttpStatus.BAD_REQUEST, request);
	}

}
