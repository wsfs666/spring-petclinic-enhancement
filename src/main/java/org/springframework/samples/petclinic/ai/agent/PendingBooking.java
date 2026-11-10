package org.springframework.samples.petclinic.ai.agent;

import java.time.LocalDateTime;
import java.util.UUID;

public record PendingBooking(Integer ownerId, Integer petId, Integer slotId, UUID idempotencyKey,
		LocalDateTime expiresAt) {

}
