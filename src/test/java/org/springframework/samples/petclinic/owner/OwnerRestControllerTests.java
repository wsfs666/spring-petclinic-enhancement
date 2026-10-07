package org.springframework.samples.petclinic.owner;

import java.beans.Transient;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

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

}