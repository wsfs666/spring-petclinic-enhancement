package org.springframework.samples.petclinic.owner;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

import static org.mockito.Answers.values;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.time.LocalDate;
import java.util.Optional;

@WebMvcTest(PetRestController.class)
public class PetRestControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OwnerRepository owners;

	@MockitoBean
	private PetTypeRepository petTypes;

	@Test
	// 1. owner exists + has pets
	// → 200
	// → response contains correct pet fields
	public void shouldReturnOwnerExistsWhenHasPets() throws Exception {
		Owner owner = new Owner();
		owner.setId(15);
		owner.setAddress("17888 Linden Ave");
		owner.setCity("Shoreline");
		owner.setFirstName("Kate");
		owner.setLastName("Li");
		owner.setTelephone("1234567891");
		Pet pet = new Pet();
		pet.setBirthDate(LocalDate.of(2026, 10, 10));

		pet.setName("Cola");
		PetType cat = new PetType();
		cat.setId(1);
		cat.setName("cat");
		pet.setType(cat);

		owner.addPet(pet);
		// when pet id is null then it can be successfully added as a new entity. or it
		// will be added as null
		// so add first ,setId then.
		pet.setId(2);

		given(this.owners.findById(15)).willReturn(Optional.of(owner));

		mockMvc.perform(get("/api/owners/{ownerId}/pets", 15))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].petId").value(pet.getId()))
			.andExpect(jsonPath("$[0].name").value(pet.getName()));

	}

	// owner exists + no pets
	// → 200
	// → []

	@Test
	public void shouldReturnOwnerExistsWhenNoPets() throws Exception {
		Owner owner = new Owner();
		owner.setId(15);
		owner.setAddress("17888 Linden Ave");
		owner.setCity("Shoreline");
		owner.setFirstName("Kate");
		owner.setLastName("Li");
		owner.setTelephone("1234567891");

		this.owners.save(owner);

		given(this.owners.findById(15)).willReturn(Optional.of(owner));

		mockMvc.perform(get("/api/owners/{ownerId}/pets", 15))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isEmpty());

	}

	// owner does not exist
	// → 404

	@Test
	public void shouldReturnOwnerNotExist() throws Exception {
		given(this.owners.findById(999)).willReturn(Optional.empty());

		mockMvc.perform(get("/api/owners/{ownerId}/pets", 999)).andExpect(status().isNotFound());
	}

	// 1. valid owner + valid pet + valid type
	// → 200 OK
	// → petId 保持不变
	// → name / birthDate / type 更新正确

	@Test
	public void shouldReturnWhenUpdateValidPet() throws Exception {
		String petRequest = """
				{

				  "petName": "Cola",
				  "birthDate": "2024-05-18",
				  "typeId": 1
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
		pet.setBirthDate(LocalDate.of(2026, 10, 10));
		pet.setName("Cold");
		PetType cat = new PetType();
		cat.setId(1);
		cat.setName("cat");
		pet.setType(cat);

		owner.addPet(pet);
		pet.setId(2);

		given(this.owners.findById(15)).willReturn(Optional.of(owner));
		given(this.petTypes.findById(1)).willReturn(Optional.of(cat));//

		mockMvc
			.perform(put("/api/owners/{ownerId}/pets/{petId}", 15, 2).contentType(MediaType.APPLICATION_JSON)
				.content(petRequest))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.petId").value(2))
			.andExpect(jsonPath("$.name").value("Cola"))
			.andExpect(jsonPath("$.birthDate").value("2024-05-18"))
			.andExpect(jsonPath("$.petTypeId").value(1));

	}

	// 2. owner 不存在
	// → 404

	@Test
	public void shouldReturnWhenOwnerNotExist() throws Exception {

		String petRequest = """
				{

				  "petName": "Cola",
				  "birthDate": "2024-05-18",
				  "typeId": 1
				}
				""";

		given(this.owners.findById(999)).willReturn(Optional.empty());

		mockMvc
			.perform(put("/api/owners/{ownerId}/pets/{petId}", 999, 2).contentType(MediaType.APPLICATION_JSON)
				.content(petRequest))
			.andExpect(status().isNotFound());

		verify(this.owners, never()).save(any(Owner.class));

	}

	// 3. invalid request body
	// → 400

	@Test
	public void shouldReturnWhenInvalidRequest() throws Exception {
		String petRequest = """
				{

				  "petName":"" ,
				  "birthDate": "2024-05-18",
				  "typeId": 1
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
		pet.setBirthDate(LocalDate.of(2026, 10, 10));
		pet.setName("Cold");
		PetType cat = new PetType();
		cat.setId(1);
		cat.setName("cat");
		pet.setType(cat);

		owner.addPet(pet);
		pet.setId(2);

		given(this.owners.findById(15)).willReturn(Optional.of(owner));
		given(this.petTypes.findById(1)).willReturn(Optional.of(cat));//

		mockMvc
			.perform(put("/api/owners/{ownerId}/pets/{petId}", 15, 2).contentType(MediaType.APPLICATION_JSON)
				.content(petRequest))
			.andExpect(status().isBadRequest());

	}

	// 4. invalid typeId
	// → 按你当前 API 约定测试 400 或 404
	@Test
	public void shouldReturnWhenInvalidTypeId() throws Exception {
		String petRequest = """
				{

				  "petName":"Cola" ,
				  "birthDate": "2024-05-18",
				  "typeId": 1
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
		pet.setBirthDate(LocalDate.of(2026, 10, 10));
		pet.setName("Cold");
		PetType cat = new PetType();
		cat.setId(1);
		cat.setName("cat");
		pet.setType(cat);

		owner.addPet(pet);
		pet.setId(2);

		given(this.owners.findById(15)).willReturn(Optional.of(owner));
		given(this.petTypes.findById(2)).willReturn(Optional.empty());// search invalid
																		// petType will
																		// return null

		mockMvc
			.perform(put("/api/owners/{ownerId}/pets/{petId}", 15, 2).contentType(MediaType.APPLICATION_JSON)
				.content(petRequest))
			.andExpect(status().isNotFound());

	}

	// 5. a pet cannot be found in a owner-> return 404

	@Test
	public void shouldReturnWhenAPetNotExist() throws Exception {
		String petRequest = """
				{

				  "petName":"Cola" ,
				  "birthDate": "2024-05-18",
				  "typeId": 1
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
		pet.setBirthDate(LocalDate.of(2026, 10, 10));
		pet.setName("Cold");
		PetType cat = new PetType();
		cat.setId(1);
		cat.setName("cat");
		pet.setType(cat);

		owner.addPet(pet);
		pet.setId(2);

		given(this.owners.findById(15)).willReturn(Optional.of(owner));
		// cannot find this pet by petId = 3

		mockMvc
			.perform(put("/api/owners/{ownerId}/pets/{petId}", 15, 3).contentType(MediaType.APPLICATION_JSON)
				.content(petRequest))
			.andExpect(status().isNotFound());

	}

	// tests on delete /api/owners/{ownerId}/pets/{petId}
	// Owner + Pet 都存在 204 No Content
	// Owner 不存在 404 Not Found
	// Owner 存在，但 Pet 不存在/不属于该 Owner 404 Not Found

	@Test
	public void shouldReturnWhenDeleteValidPet() throws Exception {
		Owner owner = new Owner();
		owner.setId(15);
		owner.setAddress("17888 Linden Ave");
		owner.setCity("Shoreline");
		owner.setFirstName("Kate");
		owner.setLastName("Li");
		owner.setTelephone("1234567891");

		Pet pet = new Pet();
		pet.setBirthDate(LocalDate.of(2026, 10, 10));
		pet.setName("Cold");
		PetType cat = new PetType();
		cat.setId(1);
		cat.setName("cat");
		pet.setType(cat);

		owner.addPet(pet);
		pet.setId(2);

		given(this.owners.findById(15)).willReturn(Optional.of(owner));

		mockMvc.perform(delete("/api/owners/{ownerId}/pets/{petId}", 15, 2)).andExpect(status().isNoContent());

	}

	@Test
	public void shouldReturnWhenNoValidOwner() throws Exception {

		given(this.owners.findById(999)).willReturn(Optional.empty());

		mockMvc.perform(delete("/api/owners/{ownerId}/pets/{petId}", 999, 2)).andExpect(status().isNotFound());
	}

	@Test
	public void shouldReturnWhenDeleteNoValidPet() throws Exception {
		Owner owner = new Owner();
		owner.setId(15);
		owner.setAddress("17888 Linden Ave");
		owner.setCity("Shoreline");
		owner.setFirstName("Kate");
		owner.setLastName("Li");
		owner.setTelephone("1234567891");

		given(this.owners.findById(15)).willReturn(Optional.of(owner));
		mockMvc.perform(delete("/api/owners/{ownerId}/pets/{petId}", 15, 2)).andExpect(status().isNotFound());

	}

}
