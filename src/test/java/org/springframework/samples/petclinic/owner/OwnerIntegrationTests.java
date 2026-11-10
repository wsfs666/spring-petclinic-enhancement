package org.springframework.samples.petclinic.owner;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.samples.petclinic.owner.Service.OwnerService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("postgres")
public class OwnerIntegrationTests {

	@Container
	@ServiceConnection

	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.3");

	@Autowired
	private OwnerRepository owners;

	@Autowired
	private MockMvc mockMvc;

	@Test

	/*
	 * 创建 Owner version = 0
	 *
	 * Client A GET → version 0
	 *
	 * Client B GET → version 0
	 *
	 * A PUT version 0 → 200 → response version 1
	 *
	 * B PUT version 0 → 409 Conflict
	 *
	 * GET Owner → 保留 A 的修改 → version 1 → B 没覆盖 A
	 *
	 */

	public void shouldUpdateOwnerAndIncrementVersion() throws Exception {

		Owner owner = new Owner();
		owner.setAddress("central way");
		owner.setCity("kirkland");
		owner.setFirstName("Forrest");
		owner.setLastName("Li");
		owner.setTelephone("1234567891");

		Owner savedOwner = this.owners.saveAndFlush(owner);

		Owner persistedOwner = owners.findById(savedOwner.getId()).orElseThrow();

		Long initialVersion = persistedOwner.getVersion();

		String updateReq = """
				{

				  "address": "Linden Ave N",
				  "city": "Shoreline",
				  "telephone": "9197715476",
				              "firstName": "Jie",
				              "lastName": "Li",
				              "version": %d

				}
				""".formatted(initialVersion);

		mockMvc
			.perform(put("/api/owners/{ownerId}", persistedOwner.getId()).contentType(MediaType.APPLICATION_JSON)
				.content(updateReq))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.address").value("Linden Ave N"));

		Owner updatedOwner = this.owners.findById(persistedOwner.getId()).orElseThrow();

		assertThat(updatedOwner.getVersion()).isEqualTo(Long.valueOf(1));

	}

	@Test
	public void shouldRejectStaleOwnerUpdate() throws Exception {

		Owner owner = new Owner();
		owner.setAddress("central way");
		owner.setCity("kirkland");
		owner.setFirstName("Forrest");
		owner.setLastName("Li");
		owner.setTelephone("1234567891");

		Owner savedOwner = this.owners.saveAndFlush(owner);

		Owner persistedOwner = owners.findById(savedOwner.getId()).orElseThrow();

		Long initialVersion = persistedOwner.getVersion();

		String updateReqA = """
				{

				  "address": "Linden Ave N",
				  "city": "Shoreline",
				  "telephone": "9197715476",
				              "firstName": "Jie",
				              "lastName": "Li",
				              "version": %d

				}
				""".formatted(initialVersion);
		String updateReqB = """
				{

				  "address": "Linden Ave N",
				  "city": "Riverside",
				  "telephone": "9197715476",
				              "firstName": "Max",
				              "lastName": "Fu",
				              "version": %d

				}
				""".formatted(initialVersion);

		mockMvc
			.perform(put("/api/owners/{ownerId}", persistedOwner.getId()).contentType(MediaType.APPLICATION_JSON)
				.content(updateReqA))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.address").value("Linden Ave N"))
			.andExpect(jsonPath("$.version").value(initialVersion.intValue() + 1));

		mockMvc
			.perform(put("/api/owners/{ownerId}", persistedOwner.getId()).contentType(MediaType.APPLICATION_JSON)
				.content(updateReqB))
			.andExpect(status().isConflict());

	}

}
