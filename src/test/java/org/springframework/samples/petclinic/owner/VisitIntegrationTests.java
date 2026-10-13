package org.springframework.samples.petclinic.owner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.PetClinicApplication;
import org.springframework.samples.petclinic.owner.Service.VisitService;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc

public class VisitIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private OwnerRepository owners;

	@Autowired
	private VisitService visitService;

	@Test
	void contextLoads() {

	}

	@Test
	void canUseRealRepository() throws Exception {
		Owner owner = new Owner();
		owner.setAddress("17888 Linden Ave");
		owner.setCity("Shoreline");
		owner.setFirstName("Kate");
		owner.setLastName("Li");
		owner.setTelephone("1234567891");

		Owner savedOwner = this.owners.save(owner);

		assertThat(savedOwner.getId()).isNotNull();

		mockMvc.perform(get("/api/owners/{ownerId}", savedOwner.getId())).andExpect(status().isOk());

	}

	/*
	 * POST /api/owners/{ownerId}/pets/{petId}/visits
	 *
	 * Expected: 201 Created Visit persisted in DB Generated ID != null Correct owner/pet
	 * relationship
	 */

	@Test
	void shouldReturnWhenVisitCreated() throws Exception {
		String vRequest = """
				{
				  "date": "2026-10-11",
				  "description": "annual body examiniation."
				}
				""";

		Owner owner = new Owner();
		owner.setAddress("17888 Linden Ave");
		owner.setCity("Shoreline");
		owner.setFirstName("Kate");
		owner.setLastName("Li");
		owner.setTelephone("1234567891");

		Pet pet = new Pet();
		pet.setBirthDate(LocalDate.of(2026, 1, 1));
		pet.setName("Cola");
		PetType cat = new PetType();
		cat.setId(1);
		cat.setName("cat");
		pet.setType(cat);
		owner.addPet(pet);

		Owner savedOwner = this.owners.save(owner);

		VisitRequest visitRequest = new VisitRequest(LocalDate.of(2026, 10, 11), "annual body examiniation.");

		this.visitService.addVisit(savedOwner.getId(), pet.getId(), visitRequest);

		// assertThat(savedOwner.getId()).isNotNull();
		// assertThat(pet.getId()).isNotNull();

		mockMvc
			.perform(post("/api/owners/{ownerId}/pets/{petId}/visits", savedOwner.getId(), pet.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(vRequest))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").isNotEmpty())
			.andExpect(jsonPath("$.petId").value(pet.getId()))
			.andExpect(jsonPath("$.ownerId").value(savedOwner.getId()));

	}

	/**
	 * GET /api/owners/{ownerId}/pets/{petId}/visits ↓ Exception： 200 get correct info in
	 * db
	 */

	@Test
	public void shouldReturnWhenGetVisits() throws Exception {

		Owner owner = new Owner();
		owner.setAddress("17888 Linden Ave");
		owner.setCity("Shoreline");
		owner.setFirstName("Kate");
		owner.setLastName("Li");
		owner.setTelephone("1234567891");
		Pet pet = new Pet();
		pet.setBirthDate(LocalDate.of(2026, 1, 1));

		pet.setName("Cola");
		PetType cat = new PetType();
		cat.setId(1);
		cat.setName("cat");
		pet.setType(cat);

		Visit visit = new Visit();
		visit.setDate(LocalDate.of(2026, 10, 11));
		visit.setDescription("annual body examiniation.");

		pet.addVisit(visit);
		owner.addPet(pet);

		Owner savedOwner = this.owners.save(owner);

		VisitRequest visitRequest = new VisitRequest(LocalDate.of(2026, 10, 11), "annual body examiniation.");

		this.visitService.addVisit(savedOwner.getId(), pet.getId(), visitRequest);

		// assertThat(savedOwner.getId()).isNotNull();
		// assertThat(pet.getId()).isNotNull();
		// assertThat(visit.getId()).isNotNull();

		mockMvc.perform(get("/api/owners/{ownerId}/pets/{petId}/visits", savedOwner.getId(), pet.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].petId").value(pet.getId()))
			.andExpect(jsonPath("$[0].ownerId").value(savedOwner.getId()))
			.andExpect(jsonPath("$[0].description").value("annual body examiniation."))
			.andExpect(jsonPath("$[0].date").value("2026-10-11"));

	}

	/*
	 * Owner DELETE Cascade Integration Test
	 *
	 * DELETE /api/owners/{ownerId}
	 *
	 * Expected： Owner: deleted Pet: deleted Visit: deleted
	 *
	 */

	/*
	 *
	 * DELETE /api/owners/{ownerId}/pets/{petId}
	 *
	 *
	 * Expected: Owner: exists Pet: deleted Visit: deleted
	 *
	 */

}
