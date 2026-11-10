package org.springframework.samples.petclinic.owner;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record PetRequest(@NotBlank String petName, LocalDate birthDate, Integer typeId) {

}
