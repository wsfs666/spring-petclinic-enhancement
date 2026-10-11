package org.springframework.samples.petclinic.owner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/owners")
public class PetRestController {

	private final PetTypeRepository petTypes;

	private final OwnerRepository owners;

	public PetRestController(PetTypeRepository petTypes, OwnerRepository owners) {
		this.petTypes = petTypes;
		this.owners = owners;
	}

	public PetResponse toPetResponse(Pet pet, int ownerId) {

		return new PetResponse(pet.getId(), pet.getName(), pet.getBirthDate(), pet.getType().getId(),
				pet.getType().getName(), ownerId);
	}

	// get pets by owner id
	@GetMapping("/{ownerId}/pets")
	public List<PetResponse> getPetsByOwnerId(@PathVariable int ownerId) {

		Optional<Owner> optionalOwner = this.owners.findById(ownerId);

		Owner owner = optionalOwner.orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found with id: " + ownerId));
		List<Pet> pets = owner.getPets();

		List<PetResponse> petResponses = new ArrayList<>();
		for (Pet pet : pets) {
			PetResponse petResponse = toPetResponse(pet, ownerId);
			petResponses.add(petResponse);
		}

		return petResponses;

	}

	// create a new pet /api/owners/{ownerId}/pets}
	@PostMapping("/{ownerId}/pets")
	public ResponseEntity<PetResponse> createNewPet(@PathVariable int ownerId,
			@Valid @RequestBody PetRequest petRequest) {
		Optional<Owner> optionalOwner = this.owners.findById(ownerId);
		Owner owner = optionalOwner.orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found with id: " + ownerId));
		Optional<PetType> optionalType = this.petTypes.findById(petRequest.typeId());
		PetType curPetType = optionalType.orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "pet not found with id" + petRequest.typeId()));
		Pet pet = new Pet();
		pet.setBirthDate(petRequest.birthDate());
		pet.setName(petRequest.petName());
		pet.setType(curPetType);
		owner.addPet(pet);

		this.owners.save(owner);

		PetResponse petResponse = new PetResponse(pet.getId(), petRequest.petName(), petRequest.birthDate(),
				curPetType.getId(), curPetType.getName(), ownerId);

		return ResponseEntity.status(HttpStatus.CREATED).body(petResponse);

	}

	// PUT /api/owners/{ownerId}/pets/{petId}

	@PutMapping("/{ownerId}/pets/{petId}")
	public ResponseEntity<PetResponse> updatePetByOwnerId(@PathVariable int ownerId, @PathVariable int petId,
			@Valid @RequestBody PetRequest petRequest) {

		Optional<Owner> optionalOwner = this.owners.findById(ownerId);
		Owner owner = optionalOwner
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "owner not found with id" + ownerId));

		Pet pet = owner.getPet(petId);
		if (pet == null)
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

		// get updated new type by new typeID
		Optional<PetType> optionalType = this.petTypes.findById(petRequest.typeId());
		PetType curPetType = optionalType.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
				"pet type not found with id" + petRequest.typeId()));

		pet.setBirthDate(petRequest.birthDate());
		pet.setName(petRequest.petName());
		pet.setType(curPetType);

		this.owners.save(owner);

		PetResponse petResponse = new PetResponse(pet.getId(), pet.getName(), pet.getBirthDate(), pet.getType().getId(),
				pet.getType().getName(), ownerId);

		return ResponseEntity.status(HttpStatus.OK).body(petResponse);

	}

}
