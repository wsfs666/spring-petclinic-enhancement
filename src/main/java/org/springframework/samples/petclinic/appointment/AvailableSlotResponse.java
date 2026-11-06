package org.springframework.samples.petclinic.appointment;

import java.time.LocalDateTime;

public record AvailableSlotResponse(Integer slotId, LocalDateTime startAt, LocalDateTime endAt) {

}
