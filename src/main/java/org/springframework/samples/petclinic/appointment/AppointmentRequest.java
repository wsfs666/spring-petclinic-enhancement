package org.springframework.samples.petclinic.appointment;

import jakarta.validation.constraints.NotNull;

public record AppointmentRequest(@NotNull Integer slotId) {

}
