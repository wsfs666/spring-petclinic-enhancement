package org.springframework.samples.petclinic.owner;

import java.time.LocalDate;

public record PetResponse(Integer petId, String name, LocalDate birthDate, Integer petTypeId, String petTypeName,
		Integer ownerId) {

}
