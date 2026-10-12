package org.springframework.samples.petclinic.owner;

import java.time.LocalDate;

public record VisitResponse(int id, LocalDate date, String description, int petId) {

}
