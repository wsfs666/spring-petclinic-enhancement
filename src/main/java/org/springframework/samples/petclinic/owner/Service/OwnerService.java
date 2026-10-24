package org.springframework.samples.petclinic.owner.Service;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.OwnerResponse;
import org.springframework.samples.petclinic.owner.UpdateOwnerRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.transaction.annotation.Transactional;

@Service
public class OwnerService {

	private final OwnerRepository owners;

	public OwnerService(OwnerRepository owners) {
		this.owners = owners;

	}

	// concurrency sceanario to update owner
	@Transactional
	public OwnerResponse updateOwner(int ownerId, UpdateOwnerRequest updateOwnerRequest) {

		Optional<Owner> optionalOwner = this.owners.findById(ownerId);
		Owner owner = optionalOwner.orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found with id: " + ownerId));

		// to see if current user is going to update the original data or updated data
		// version: owner.getVersion(). current: createOwnerRequest.version()

		if (!owner.getVersion().equals(updateOwnerRequest.version())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "already updated by someone else.");
		}

		owner.setAddress(updateOwnerRequest.address());
		owner.setCity(updateOwnerRequest.city());
		owner.setFirstName(updateOwnerRequest.firstName());
		owner.setLastName(updateOwnerRequest.lastName());
		owner.setTelephone(updateOwnerRequest.telephone());

		owners.flush();

		OwnerResponse ownerResponse = new OwnerResponse(owner.getAddress(), owner.getCity(), owner.getTelephone(),
				owner.getFirstName(), owner.getLastName(), owner.getId(), owner.getVersion());

		return ownerResponse;

	}

}
