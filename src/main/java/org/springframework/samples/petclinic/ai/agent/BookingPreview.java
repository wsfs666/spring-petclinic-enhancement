package org.springframework.samples.petclinic.ai.agent;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookingPreview(UUID confirmationId, Integer petId, Integer slotId, LocalDateTime startAt,
		LocalDateTime endAt, LocalDateTime expiresAt) {

}
