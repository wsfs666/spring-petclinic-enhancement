package org.springframework.samples.petclinic.appointment;

import java.time.LocalDateTime;

public record AppointmentResponse(Integer id, Integer petId, Integer slotId, LocalDateTime startAt, LocalDateTime endAt,
		AppointmentStatus status, LocalDateTime createdAt) {

}
