package org.springframework.samples.petclinic.appointment;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import jakarta.validation.Valid;

@RestController
public class AppointmentRestController {

	private AppointmentService appointmentService;

	private IdempotencyService idempotencyService;

	public AppointmentRestController(AppointmentService appointmentService, IdempotencyService idempotencyService) {
		this.appointmentService = appointmentService;
		this.idempotencyService = idempotencyService;
	}

	@PostMapping("/api/pets/{petId}/appointments")
	public ResponseEntity<AppointmentResponse> createAppointment(@PathVariable("petId") Integer petId,
			@Valid @RequestBody AppointmentRequest appointmentRequest,
			@RequestHeader("Idempotency-Key") UUID idempotencyKey) {

		return ResponseEntity.status(HttpStatus.CREATED)
			.body(this.idempotencyService.createAppointmentIdempotently(petId, appointmentRequest, idempotencyKey));

	}

	@GetMapping("/api/appointments/{appointmentId}")
	public ResponseEntity<AppointmentResponse> getAppointment(@PathVariable("appointmentId") Integer appointmentId) {
		return ResponseEntity.status(HttpStatus.OK).body(this.appointmentService.getAppointmentById(appointmentId));
	}

}
