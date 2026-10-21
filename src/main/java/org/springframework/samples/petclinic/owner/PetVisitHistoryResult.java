package org.springframework.samples.petclinic.owner;

import java.util.List;

public record PetVisitHistoryResult(int petId, String petName, List<VisitResponse> visits) {

}
