package org.springframework.samples.petclinic.appointment;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AppointmentExceptionHandler {

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<String> handle(Exception ex) {

		Throwable current = ex;
		while (current != null) {
			if (current instanceof org.hibernate.exception.ConstraintViolationException cve
					&& "uq_confirmed_appointment_slot".equals(cve.getConstraintName())) {

				return ResponseEntity.status(HttpStatus.CONFLICT).body("double booked");

			}

			current = current.getCause();
		}

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("request failed");

	}

}
