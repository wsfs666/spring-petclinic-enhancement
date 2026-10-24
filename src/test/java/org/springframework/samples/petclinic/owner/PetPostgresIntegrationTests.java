package org.springframework.samples.petclinic.owner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.print.attribute.standard.Media;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.springframework.web.server.ResponseStatusException;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.HttpStatus;

import org.testcontainers.junit.jupiter.Container;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("postgres")
public class PetPostgresIntegrationTests {

	@Container
	@ServiceConnection

	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.3");

	@Autowired
	private OwnerRepository owners;

	@Autowired
	private MockMvc mockMvc;

	@Test
	/*
	 * POST /api/owners/{ownerId}/pets → 201 / success → response.petId != null → DB
	 * 中真的存在这个 Pet → owner relationship 正确
	 */

	public void shouldReturnWhenCreateValidPet() throws Exception {
		String petRequest = """
				{

				  "petName": "Cola",
				  "birthDate": "2024-05-18",
				  "typeId": 1
				}
				""";
		Owner owner = new Owner();
		owner.setAddress("17888 Linden Ave N");
		owner.setCity("shoreline");
		owner.setFirstName("Jie");
		owner.setLastName("Li");
		owner.setTelephone("9197715476");

		// add owner to postgres
		Owner savedOwner = this.owners.save(owner);

		Owner persistedOwner = owners.findById(savedOwner.getId()).orElseThrow();

		assertThat(persistedOwner.getFirstName()).isEqualTo("Jie");
		assertThat(persistedOwner.getId()).isNotNull();

		mockMvc
			.perform(post("/api/owners/{ownerId}/pets", persistedOwner.getId()).contentType(MediaType.APPLICATION_JSON)
				.content(petRequest))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.petId").isNumber())
			.andExpect(jsonPath("$.name").value("Cola"))
			.andExpect(jsonPath("$.birthDate").value("2024-05-18"));

	}

}
