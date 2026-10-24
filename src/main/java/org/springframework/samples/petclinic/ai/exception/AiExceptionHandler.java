package org.springframework.samples.petclinic.ai.exception;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.ai.retry.TransientAiException;

import java.net.SocketTimeoutException;

import org.springframework.ai.retry.NonTransientAiException;
import com.openai.errors.OpenAIIoException;

@RestControllerAdvice
public class AiExceptionHandler {

	@ExceptionHandler(NonTransientAiException.class) // return 500
	public ResponseEntity<String> handleNonTransientAiException(NonTransientAiException ex) {

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("AI service requests failed");
	}

	@ExceptionHandler(TransientAiException.class)
	public ResponseEntity<String> handleTimeOutException(TransientAiException ex) {

		if (hasCause(ex, SocketTimeoutException.class)) {
			return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body("AI provider request timeout");
		}

		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("AI service temporarily unavailable");

	}

	boolean hasCause(Throwable exception, Class<? extends Throwable> targetType) {

		Throwable current = exception;
		while (current != null) {
			if (targetType.isInstance(current)) {
				return true;
			}

			current = current.getCause();
		}

		return false;
	}

}
