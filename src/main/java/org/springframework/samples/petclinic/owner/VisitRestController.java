package org.springframework.samples.petclinic.owner;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/owners")
public class VisitRestController {

	private final OwnerRepository owners;

	public VisitRestController(OwnerRepository owners) {
		this.owners = owners;
	}

	@GetMapping("/{ownerId}/pets/{petId}/visits")
	public ResponseEntity<List<VisitResponse>> getVisits(@PathVariable("ownerId") int ownerId,
			@PathVariable("petId") int petId) {
		Optional<Owner> optionalOwner = this.owners.findById(ownerId);
		Owner owner = optionalOwner
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "owner not found with id" + ownerId));

		Pet pet = owner.getPet(petId);
		if (pet == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}

		Collection<Visit> visits = pet.getVisits();

		List<VisitResponse> visitResponses = new ArrayList<>();

		for (Visit visit : visits) {
			VisitResponse vResponse = new VisitResponse(visit.getId(), visit.getDate(), visit.getDescription(), petId);
			visitResponses.add(vResponse);
		}

		return ResponseEntity.ok().body(visitResponses);

	}

	@PostMapping("/{ownerId}/pets/{petId}/visits")
	public ResponseEntity<VisitResponse> addVisit(@PathVariable("ownerId") int ownerId,
			@PathVariable("petId") int petId, @Valid @RequestBody VisitRequest visitRequest) {

		Optional<Owner> optionalOwner = this.owners.findById(ownerId);
		Owner owner = optionalOwner
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "owner not found with id" + ownerId));

		Pet pet = owner.getPet(petId);
		if (pet == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}

		Visit visit = new Visit();
		visit.setDate(visitRequest.date());
		visit.setDescription(visitRequest.description());

		pet.addVisit(visit);
		this.owners.save(owner);

		VisitResponse visitResponse = new VisitResponse(visit.getId(), visitRequest.date(), visitRequest.description(),
				petId);

		return ResponseEntity.status(HttpStatus.CREATED).body(visitResponse);

	}

}
