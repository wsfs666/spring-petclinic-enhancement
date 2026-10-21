package org.springframework.samples.petclinic.owner.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetVisitHistoryResult;
import org.springframework.samples.petclinic.owner.Visit;
import org.springframework.samples.petclinic.owner.VisitRequest;
import org.springframework.samples.petclinic.owner.VisitResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VisitService {

	private final OwnerRepository owners;

	public VisitService(OwnerRepository owners) {
		this.owners = owners;
	}

	@Transactional(readOnly = true)
	public List<VisitResponse> getVisits(int ownerId, int petId) {
		Optional<Owner> optionalOwner = this.owners.findById(ownerId);
		Owner owner = optionalOwner
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "owner not found with id" + ownerId));

		Pet pet = owner.getPet(petId);

		if (pet == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found with id " + petId);
		}

		Collection<Visit> visits = pet.getVisits();

		List<VisitResponse> visitResponses = new ArrayList<>();

		for (Visit visit : visits) {
			VisitResponse vResponse = new VisitResponse(visit.getId(), visit.getDate(), visit.getDescription(), petId,
					ownerId);
			visitResponses.add(vResponse);
		}

		return visitResponses;

	}

	@Transactional
	public VisitResponse addVisit(int ownerId, int petId, VisitRequest visitRequest) {
		// find owner
		// find pet
		// create Visit
		// pet.addVisit(visit)
		// flush if response immediately needs generated ID
		// return VisitResponse

		Optional<Owner> optionalOwner = this.owners.findById(ownerId);
		Owner owner = optionalOwner
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "owner not found with id" + ownerId));

		Pet pet = owner.getPet(petId);
		if (pet == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found with id " + petId);
		}

		Visit visit = new Visit();
		visit.setDate(visitRequest.date());
		visit.setDescription(visitRequest.description());

		pet.addVisit(visit);
		this.owners.flush();

		VisitResponse visitResponse = new VisitResponse(visit.getId(), visitRequest.date(), visitRequest.description(),
				petId, ownerId);

		return visitResponse;

	}

	@Transactional(readOnly = true)
	public PetVisitHistoryResult getVisitsByPetId(int petId) {

		Optional<Owner> opOwner = this.owners.findOwnerByPetId(petId);
		Owner owner = opOwner
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "pet not found with petId" + petId));

		Pet pet = owner.getPet(petId);

		String petName = pet.getName();

		Collection<Visit> visits = pet.getVisits();

		List<VisitResponse> visitResponses = new ArrayList<>();

		for (Visit visit : visits) {
			VisitResponse vResponse = new VisitResponse(visit.getId(), visit.getDate(), visit.getDescription(), petId,
					owner.getId());
			visitResponses.add(vResponse);
		}

		return new PetVisitHistoryResult(petId, petName, visitResponses);

	}

}
