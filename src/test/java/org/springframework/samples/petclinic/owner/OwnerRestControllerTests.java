package org.springframework.samples.petclinic.owner;

import java.beans.Transient;
import java.util.Optional;

import org.assertj.core.condition.AnyOf;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.handler.HandlerExceptionResolverComposite;

import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.owner.Service.OwnerService;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@WebMvcTest(OwnerRestController.class)
// When you use @WebMvcTest, it only loads the web layer (the controller, filters, etc.),
// not the entire application context. This makes the tests run faster
// since it doesn't include things like services, repositories, etc.
public class OwnerRestControllerTests {

	@Autowired
	// is used to inject dependencies automatically.
	// In the context of a test, it is typically used to inject the MockMvc object,
	// which allows you to simulate HTTP requests to the controller.
	private MockMvc mockMvc;

	@MockitoBean
	// @MockitoBean is an annotation that can be used in test classes to override a bean
	// in the test's ApplicationContext with a Mockito mock.
	private OwnerRepository owners;

	@MockitoBean

	private OwnerService ownerService;

	@Test
	// @Test: Indicated that the method is a test case.
	public void shouldReturnOwnerbyId() throws Exception {

		Owner owner = new Owner();
		owner.setId(17);
		owner.setAddress("17888 Linden Ave");
		owner.setCity("Shoreline");
		owner.setFirstName("Kate");
		owner.setLastName("Li");
		owner.setTelephone("1234567891");

		given(this.owners.findById(17)).willReturn(Optional.of(owner));

		// Act + Assert

		mockMvc.perform(get("/api/owners/{ownerId}", 17))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(17))
			.andExpect(jsonPath("$.firstName").value("Kate"));

	}

	@Test
	public void shouldReturnNoFoundWhenOwnerDoesNotExist() throws Exception {

		given(this.owners.findById(999)).willReturn(Optional.empty());

		mockMvc.perform(get("/api/owners/{ownerId}", 999)).andExpect(status().isNotFound());
	}

	@Test
	public void shouldReturnOwnerPageByLastname() throws Exception {
		Owner owner = new Owner();
		owner.setId(17);
		owner.setAddress("17888 Linden Ave");
		owner.setCity("Shoreline");
		owner.setFirstName("Kate");
		owner.setLastName("Li");
		owner.setTelephone("1234567891");

		Page<Owner> page = new PageImpl<>(List.of(owner));

		given(this.owners.findByLastNameStartingWith(eq("Li"), any(Pageable.class))).willReturn(page);

		mockMvc.perform(get("/api/owners?lastName={lastName}", "Li")).andExpect(status().isOk());

	}

	@Test
	// 验收： 合法请求可以创建 Owner，响应包含数据库生成的 ID，字段映射正确。
	public void shouldReturnCreatedOwner() throws Exception {

		/// Arrange
		String requestBody = """
				{
				  "firstName": "Amy",
				  "lastName": "Smith",
				  "address": "123 Main St",
				  "city": "Seattle",
				  "telephone": "1234567890"
				}
				""";

		Owner savedOwner = new Owner();
		savedOwner.setAddress("123 Main St");
		savedOwner.setCity("Seattle");
		savedOwner.setFirstName("Amy");
		savedOwner.setLastName("Smith");
		savedOwner.setTelephone("1234567890");
		savedOwner.setId(13);

		// if this method is called , safedOwner will return
		given(this.owners.save(any(Owner.class))).willReturn(savedOwner);

		// verify(owners).save(any(Owner.class));

		// Request validation failed return 400 Bad Request
		// when it is illegal requesting, repository.save() not callable

		mockMvc.perform(post("/api/owners").contentType(MediaType.APPLICATION_JSON).content(requestBody))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.city").value("Seattle"))
			.andExpect(jsonPath("$.id").value(13));

	}

	@Test
	public void shouldReturnWhenFirstNameIsBlank() throws Exception {
		// Request validation failed return 400 Bad Request
		String FirstNameIsBlank = """
				{
				  "firstName": "",
				  "lastName": "Smith",
				  "address": "123 Main St",
				  "city": "Seattle",
				  "telephone": "1234567890"
				}
				""";

		mockMvc.perform(post("/api/owners").contentType(MediaType.APPLICATION_JSON).content(FirstNameIsBlank))
			.andExpect(status().isBadRequest());

		verify(owners, never()).save(any(Owner.class));

	}

	// id not exist testing

	@Test
	public void shouldReturnWhenIdNotExist() throws Exception {

		String requestBody = """
				{
				  "firstName": "Amy",
				  "lastName": "Smith",
				  "address": "123 Main St",
				  "city": "Seattle",
				  "telephone": "1234567890",
				  "version": 0
				}
				""";

		// given(this.owners.findById(999)).willReturn(Optional.empty());
		given(this.ownerService.updateOwner(eq(999), any(UpdateOwnerRequest.class)))
			.willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "owner not found"));

		mockMvc.perform(put("/api/owners/{ownerId}", 999).contentType(MediaType.APPLICATION_JSON).content(requestBody))
			.andExpect(status().isNotFound());

	}

	// id exist, illegal update testing
	@Test
	public void shouldReturnWhenIllegalUpdate() throws Exception {

		String requestBody = """
				{
				  "firstName": "",
				  "lastName": "Smith",
				  "address": "123 Main St",
				  "city": "Seattle",
				  "telephone": "1234567890"
				}
				""";

		mockMvc.perform(put("/api/owners/{ownerId}", 13).contentType(MediaType.APPLICATION_JSON).content(requestBody))
			.andExpect(status().isBadRequest());

	}

	// test delete api/owners/{ownerId}
	// owner exist return no_content
	// owner not exist return no_found
	@Test
	public void shouldReturnWhenDeleteExistOwner() throws Exception {
		Owner owner = new Owner();
		owner.setId(17);
		owner.setAddress("17888 Linden Ave");
		owner.setCity("Shoreline");
		owner.setFirstName("Kate");
		owner.setLastName("Li");
		owner.setTelephone("1234567891");

		given(this.owners.findById(17)).willReturn(Optional.of(owner));

		mockMvc.perform(delete("/api/owners/{ownerId}", 17)).andExpect(status().isNoContent());

	}

	@Test
	public void shouldReturnWhenDeleteNoExistOwner() throws Exception {

		given(this.owners.findById(999)).willReturn(Optional.empty());
		mockMvc.perform(delete("/api/owners/{ownerId}", 999)).andExpect(status().isNotFound());
	}

}