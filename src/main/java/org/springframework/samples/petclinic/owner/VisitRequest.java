package org.springframework.samples.petclinic.owner;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;

public record VisitRequest(LocalDate date, @NotBlank String description) {

}
