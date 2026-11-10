package org.springframework.samples.petclinic.owner;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.owner.Service.VisitService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/owners")
public class VisitRestController {

	// private final OwnerRepository owners;
	private final VisitService visitService;

	public VisitRestController(VisitService visitService) {
		// this.owners = owners;
		this.visitService = visitService;
	}

	@GetMapping("/{ownerId}/pets/{petId}/visits")
	public ResponseEntity<List<VisitResponse>> getVisits(@PathVariable("ownerId") int ownerId,
			@PathVariable("petId") int petId) {
		/*
		 * Optional<Owner> optionalOwner = this.owners.findById(ownerId); Owner owner =
		 * optionalOwner .orElseThrow(() -> new
		 * ResponseStatusException(HttpStatus.NOT_FOUND, "owner not found with id" +
		 * ownerId));
		 *
		 * Pet pet = owner.getPet(petId); if (pet == null) { return
		 * ResponseEntity.status(HttpStatus.NOT_FOUND).build(); }
		 *
		 * Collection<Visit> visits = pet.getVisits();
		 *
		 * List<VisitResponse> visitResponses = new ArrayList<>();
		 *
		 * for (Visit visit : visits) { VisitResponse vResponse = new
		 * VisitResponse(visit.getId(), visit.getDate(), visit.getDescription(), petId,
		 * ownerId); visitResponses.add(vResponse); }
		 */

		List<VisitResponse> visitResponses = this.visitService.getVisits(ownerId, petId);

		return ResponseEntity.ok().body(visitResponses);

	}

	// @Transactional
	/**
	 *
	 * @Transactional 给一整个业务操作建立同一个 persistence context， 让 JPA entity 在这段过程中保持 managed。
	 * @param ownerId
	 * @param petId
	 * @param visitRequest
	 * @return
	 */
	@PostMapping("/{ownerId}/pets/{petId}/visits")
	public ResponseEntity<VisitResponse> addVisit(@PathVariable("ownerId") int ownerId,
			@PathVariable("petId") int petId, @Valid @RequestBody VisitRequest visitRequest) {

		/*
		 * Optional<Owner> optionalOwner = this.owners.findById(ownerId); Owner owner =
		 * optionalOwner .orElseThrow(() -> new
		 * ResponseStatusException(HttpStatus.NOT_FOUND, "owner not found with id" +
		 * ownerId));
		 *
		 * Pet pet = owner.getPet(petId); if (pet == null) { return
		 * ResponseEntity.status(HttpStatus.NOT_FOUND).build(); }
		 *
		 * Visit visit = new Visit(); visit.setDate(visitRequest.date());
		 * visit.setDescription(visitRequest.description());
		 *
		 * pet.addVisit(visit); this.owners.flush();
		 *
		 * // ######## /**
		 *
		 * save() → 告诉 persistence context：这个状态需要保存 → 不一定立即执行 SQL
		 *
		 *
		 *
		 *
		 * saveAndFlush() → 保存 → 立即 flush → INSERT Visit → database generates ID →
		 * visit.getId() 有值
		 */

		VisitResponse visitResponse = this.visitService.addVisit(ownerId, petId, visitRequest);

		return ResponseEntity.status(HttpStatus.CREATED).body(visitResponse);

	}

}
