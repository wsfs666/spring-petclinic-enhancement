package org.springframework.samples.petclinic.owner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.OpenOption;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(VisitRestController.class)
public class VisitRestControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OwnerRepository owners;

	// GET visits — owner + pet + visits exist
	// return200 OK
	// check 正确的 visitId / date / description / petId / ownerId

	@Test
	public void shouldReturnWhenGetValidVisits() throws Exception {
		Owner owner = new Owner();
		owner.setId(15);
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
		pet.setId(2);
		visit.setId(1);

		given(this.owners.findById(15)).willReturn(Optional.of(owner));

		mockMvc.perform(get("/api/owners/{ownerId}/pets/{petId}/visits", 15, 2))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].id").value(visit.getId()))
			.andExpect(jsonPath("$[0].date").value("2026-10-11"))
			.andExpect(jsonPath("$[0].description").value(visit.getDescription()))
			.andExpect(jsonPath("$[0].petId").value(pet.getId()));

	}

	/*
	 * GET visits — pet 存在但没有 visits - 200 OK - 返回 [] - 这个很重要，专门验证你刚改掉的“empty visits 不应该
	 * 404”
	 */
	@Test
	public void shouldReturnWhenPetExistsWithNoVisits() throws Exception {
		Owner owner = new Owner();
		owner.setId(15);
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
		pet.setId(2);

		given(this.owners.findById(15)).willReturn(Optional.of(owner));
		mockMvc.perform(get("/api/owners/{ownerId}/pets/{petId}/visits", 15, 2)).andExpect(status().isOk());

	}

	/*
	 * GET visits — owner 不存在 - 404 Not Found
	 */
	@Test
	public void shouldReturnWhenNoValidOwner() throws Exception {
		given(this.owners.findById(999)).willReturn(Optional.empty());
		mockMvc.perform(get("/api/owners/{ownerId}/pets/{petId}/visits", 999, 2)).andExpect(status().isNotFound());
	}

	/*
	 * GET visits — owner 存在，但 pet 不存在 / 不属于该 owner - 404 Not Found
	 */
	@Test
	public void shouldReturnWhenNoPetExists() throws Exception {
		Owner owner = new Owner();
		owner.setId(15);
		owner.setAddress("17888 Linden Ave");
		owner.setCity("Shoreline");
		owner.setFirstName("Kate");
		owner.setLastName("Li");
		owner.setTelephone("1234567891");

		given(this.owners.findById(15)).willReturn(Optional.of(owner));
		mockMvc.perform(get("/api/owners/{ownerId}/pets/{petId}/visits", 15, 2)).andExpect(status().isNotFound());

	}

	/*
	 * POST visit — valid request - 201 Created - response 中 date / description / petId /
	 * ownerId 正确 - 验证 owners.save(owner) 被调用 - 如果你断言 visitId，记住 Mockito 不会自动生成 ID，需要自己处理
	 * mock test data
	 */

	@Test
	public void shouldReturnWhenValidRequest() throws Exception {
		String vRequest = """
				{
				  "date": "2026-10-11",
				  "description": "annual body examiniation."
				}
				""";
		Owner owner = new Owner();
		owner.setId(15);
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
		pet.setId(2);
		this.owners.save(owner);

		given(this.owners.findById(15)).willReturn(Optional.of(owner));

		given(this.owners.save(any(Owner.class))).willAnswer(invocation -> {
			Owner savedOwner = invocation.getArgument(0);

			Visit savedVisit = savedOwner.getPet(2).getVisits().iterator().next();

			savedVisit.setId(1);

			return savedOwner;
		});

		mockMvc
			.perform(post("/api/owners/{owerId}/pets/{petId}/visits", 15, 2).contentType(MediaType.APPLICATION_JSON)
				.content(vRequest))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.date").value("2026-10-11"))
			.andExpect(jsonPath("$.description").value("annual body examiniation."))
			.andExpect(jsonPath("$.petId").value(2));

	}

	/*
	 * POST visit — owner 不存在 - 404 Not Found - verify(owners, never()).save(any())
	 */
	@Test
	public void shouldReturnWhenOwnerNoExists() throws Exception {
		String vRequest = """
				{
				  "date": "2024-05-18",
				  "description": "annual examination."
				}
				""";
		Owner owner = new Owner();
		owner.setId(15);
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

		given(this.owners.findById(999)).willReturn(Optional.empty());

		mockMvc
			.perform(post("/api/owners/{ownerId}/pets/{petId}/visits", 999, 2).contentType(MediaType.APPLICATION_JSON)
				.content(vRequest))
			.andExpect(status().isNotFound());
		verify(this.owners, never()).save(any(Owner.class));
	}

	/*
	 * POST visit — invalid request - 400 Bad Request - 比如 description blank / date
	 * null，取决于你的 VisitRequest validation - 同样验证 save() 没有被调用
	 */
	@Test
	public void shouldReturnWhenCreateInvalidRequest() throws Exception {

		String vRequest = """
				{
				  "date": "2024-05-18",
				  "description": ""
				}
				""";
		Owner owner = new Owner();
		owner.setId(15);
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
		pet.setId(2);

		given(this.owners.findById(15)).willReturn(Optional.of(owner));

		mockMvc
			.perform(post("/api/owners/{ownerId}/pets/{petId}/visits", 15, 2).contentType(MediaType.APPLICATION_JSON)
				.content(vRequest))
			.andExpect(status().isBadRequest());

	}

	/*
	 * POST visit — pet 不存在 / 不属于 owner → 404 → save() never called
	 */

	@Test
	public void shouldReturnWhenPetsNoExist() throws Exception {

		String vRequest = """
				{
				  "date": "2024-05-18",
				  "description": "annual examination."
				}
				""";
		Owner owner = new Owner();
		owner.setId(15);
		owner.setAddress("17888 Linden Ave");
		owner.setCity("Shoreline");
		owner.setFirstName("Kate");
		owner.setLastName("Li");
		owner.setTelephone("1234567891");

		given(this.owners.findById(15)).willReturn(Optional.of(owner));

		mockMvc
			.perform(post("/api/owners/{ownerId}/pets/{petId}/visits", 15, 2).contentType(MediaType.APPLICATION_JSON)
				.content(vRequest))
			.andExpect(status().isNotFound());

		verify(this.owners, never()).save(any(Owner.class));

	}

}
