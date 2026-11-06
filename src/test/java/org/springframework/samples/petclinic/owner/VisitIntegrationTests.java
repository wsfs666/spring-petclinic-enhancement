package org.springframework.samples.petclinic.owner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.flywaydb.database.postgresql.PostgreSQLConnection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.PetClinicApplication;
import org.springframework.samples.petclinic.owner.Service.VisitService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("postgres")
public class VisitIntegrationTests {

	@Container
	@ServiceConnection

	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.3");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private OwnerRepository owners;

	@Autowired
	private VisitService visitService;

	@Autowired
	private PetTypeRepository petTypes;

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

		Owner persistOwner = this.owners.findById(savedOwner.getId()).orElseThrow();

		assertThat(persistOwner.getId()).isNotNull();

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
		PetType cat = this.petTypes.findById(1).orElseThrow();
		pet.setType(cat);
		owner.addPet(pet);

		Owner savedOwner = this.owners.saveAndFlush(owner);

		Owner persisOwner = this.owners.findById(savedOwner.getId()).orElseThrow();

		// no need use add function , add by restAPI method and will call add service
		// function

		assertThat(persisOwner.getId()).isNotNull();
		assertThat(pet.getId()).isNotNull();
		assertThat(persisOwner.getCity()).isEqualTo("Shoreline");

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
		PetType cat = this.petTypes.findById(1).orElseThrow();
		pet.setType(cat);

		owner.addPet(pet);

		Owner savedOwner = this.owners.saveAndFlush(owner);

		VisitRequest visitRequest = new VisitRequest(LocalDate.of(2026, 10, 11), "annual body examiniation.");

		// add first then can retrive
		this.visitService.addVisit(savedOwner.getId(), pet.getId(), visitRequest);

		Owner persisOwner = this.owners.findById(savedOwner.getId()).orElseThrow();

		assertThat(persisOwner.getId()).isNotNull();
		assertThat(pet.getId()).isNotNull();
		assertThat(persisOwner.getCity()).isEqualTo("Shoreline");

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
