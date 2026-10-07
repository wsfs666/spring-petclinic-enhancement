package org.springframework.samples.petclinic.owner;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/owners")
public class OwnerRestController {

	private final OwnerRepository owners;

	public OwnerRestController(OwnerRepository owners) {
		this.owners = owners;
	}

	@GetMapping("/{ownerId}")
	public OwnerResponse getOwner(@PathVariable("ownerId") int ownerId) {
		// Used to identify a specific resource directly inside the URL path (e.g., /users/101)

		Optional<Owner> optionalOwner = this.owners.findById(ownerId);
		Owner owner = optionalOwner.orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found with id: " + ownerId));
		OwnerResponse ownerResponse = new OwnerResponse(owner.getAddress(), owner.getCity(), owner.getTelephone(),
				owner.getFirstName(), owner.getLastName(), owner.getId());

		return ownerResponse;

	}

	@GetMapping
	
	public Page<OwnerResponse> getOwnerByLastName(@RequestParam String lastName,
			@RequestParam(defaultValue = "1") int page) {
                // this is query
		int pageSize = 5;
		Pageable pageable = PageRequest.of(page - 1, pageSize);

		Page<Owner> ownersPage = this.owners.findByLastNameStartingWith(lastName, pageable);
		Page<OwnerResponse> responsesPage = ownersPage.map(owner -> new OwnerResponse(owner.getAddress(),
				owner.getCity(), owner.getTelephone(), owner.getFirstName(), owner.getLastName(), owner.getId()));
		// Page inherited methods from Streamable interface
		// it Returns a new Page with the content of the current one mapped by the map
		// function

		return responsesPage;

	}

}