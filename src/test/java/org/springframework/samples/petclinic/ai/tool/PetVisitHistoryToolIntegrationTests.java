package org.springframework.samples.petclinic.ai.tool;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.owner.PetTypeRepository;
import org.springframework.samples.petclinic.owner.PetVisitHistoryResult;
import org.springframework.samples.petclinic.owner.Visit;
import org.springframework.samples.petclinic.owner.VisitRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.reset;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@ActiveProfiles("postgres")
class PetVisitHistoryToolIntegrationTests {

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.3");

	@Autowired
	PetVisitHistoryTool petVisitHistoryTool;

	// repository if you need test data setup
	@Autowired
	OwnerRepository owners;

	@Autowired
	PetTypeRepository petTypes;

	@Test
	// pet and visits both exist
	public void shouldReturnWhenPetAndVisitExist() {

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

		Visit visit = new Visit();
		visit.setDate(LocalDate.of(2026, 10, 11));
		visit.setDescription("annual body examiniation.");

		pet.addVisit(visit);

		Owner savedOwner = this.owners.saveAndFlush(owner);

		Owner persisOwner = this.owners.findById(savedOwner.getId()).orElseThrow();

		int petId = pet.getId();

		PetVisitHistoryResult result = this.petVisitHistoryTool.getPetVisitHistory(petId);

		assertThat(result.petId()).isEqualTo(pet.getId());
		assertThat(result.petName()).isEqualTo("Cola");
		// assertThat(result.visits()).isEqualTo(visit);

	}

	// pet exists but no visit

	@Test
	public void shouldReturnWhenPetExistsAndNoVisit() {
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

		PetVisitHistoryResult result = this.petVisitHistoryTool.getPetVisitHistory(pet.getId());

		assertThat(result.visits()).isEmpty();
		assertThat(result.petId()).isEqualTo(pet.getId());
		assertThat(result.petName()).isEqualTo("Cola");

	}
	// pet not found

	@Test
	public void shouldReturnWhenPetNotFound() {
		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> this.petVisitHistoryTool.getPetVisitHistory(999));

		assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

}