package org.springframework.samples.petclinic.appointment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.PetType;
import org.springframework.samples.petclinic.owner.PetTypeRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.setRemoveAssertJRelatedElementsFromStackTrace;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("postgres")
public class AppointmentIntegrationTests {

	@Container
	@ServiceConnection

	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.3");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private AppointmentRepository appointments;

	@Autowired
	private OwnerRepository owners;

	@Autowired
	private AppointmentSlotRepository slots;

	@Autowired
	private ObjectMapper objectMapper;

	// shouldCreateAppointment() return 200. /api/pets/{petId}/appointments

	@Test
	public void shouldCreateAppointment() throws Exception {

		/*
		 * Owner owner = new Owner(); owner.setAddress("central way");
		 * owner.setCity("kirkland"); owner.setFirstName("Forrest");
		 * owner.setLastName("Li"); owner.setTelephone("1234567891");
		 */
		int petId = 1;

		Pet pet = this.owners.findPetById(petId).orElseThrow();

		AppointmentSlot slot = new AppointmentSlot();

		slot.setStartAt(LocalDateTime.of(2026, 10, 26, 10, 20));
		slot.setEndAt(LocalDateTime.of(2026, 10, 26, 10, 50));

		AppointmentSlot savedSlot = this.slots.saveAndFlush(slot);

		AppointmentRequest request = new AppointmentRequest(savedSlot.getId());

		mockMvc
			.perform(post("/api/pets/{petId}/appointments", petId)
				.header("Idempotency-Key", UUID.randomUUID().toString())
				.contentType(MediaType.APPLICATION_JSON)

				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.petId").value(petId))
			.andExpect(jsonPath("$.status").value("CONFIRMED"));

	}

	// get appointment by id get /api/appointments/{appointmentId}
	@Test
	public void shouldReturnAppointment() throws Exception {
		int petId = 1;

		Pet pet = this.owners.findPetById(petId).orElseThrow();

		AppointmentSlot slot = new AppointmentSlot();

		slot.setStartAt(LocalDateTime.of(2026, 10, 26, 13, 20)); // slot 不能重复被占用
																	// 所以应该跟上一个method创建不同的slot
		slot.setEndAt(LocalDateTime.of(2026, 10, 26, 13, 50));

		AppointmentSlot savedSlot = this.slots.saveAndFlush(slot);

		Appointment appointment = new Appointment();
		appointment.setPet(pet);
		appointment.setSlot(savedSlot);

		Appointment savedAppointment = this.appointments.saveAndFlush(appointment);

		mockMvc.perform(get("/api/appointments/{appointmentId}", savedAppointment.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.petId").value(petId));

	}

	// shouldReturn404WhenAppointmentNotFound() get /api/appointments/{appointmentId}
	// return 404

	@Test
	public void shouldReturn404WhenAppointmentNotFound() throws Exception {

		mockMvc.perform(get("/api/appointmnts/{appointmentId}", 7)).andExpect(status().isNotFound());

	}

	// shouldReturn404WhenPetNotFound() post /api/pets/{petId}/appointments
	@Test
	public void shouldReturn404WhenPetNotFound() throws Exception {

		int petId = 999;
		AppointmentSlot slot = new AppointmentSlot();

		slot.setStartAt(LocalDateTime.of(2026, 10, 27, 10, 20));
		slot.setEndAt(LocalDateTime.of(2026, 10, 27, 10, 50));

		AppointmentSlot savedSlot = this.slots.saveAndFlush(slot);

		AppointmentRequest request = new AppointmentRequest(savedSlot.getId());

		mockMvc
			.perform(post("/api/pets/{petId}/appointments", petId)
				.header("Idempotency-Key", UUID.randomUUID().toString())
				.contentType(MediaType.APPLICATION_JSON)

				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound());

	}

	// shouldReturn404WhenSlotNotFound() post /api/pets/{petId}/appointments
	@Test
	public void shouldReturn404WhenSlotNotFound() throws Exception {

		int petId = 1;
		AppointmentRequest request = new AppointmentRequest(999);
		mockMvc
			.perform(post("/api/pets/{petId}/appointments", petId)
				.header("Idempotency-Key", UUID.randomUUID().toString())
				.contentType(MediaType.APPLICATION_JSON)

				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound());

	}

	// shouldRejectNullSlotId() 400 post /api/pets/{petId}/appointments
	public void shouldRejectNullSlotId() throws Exception {
		int petId = 1;
		String request = """
				{
				  "slotId":
				}
				""";

		mockMvc
			.perform(post("/api/pets/{petId}/appointments", petId).contentType(MediaType.APPLICATION_JSON)
				.content(request))
			.andExpect(status().isBadRequest());

	}

	// shouldRejectDoubleBooking() post /api/pets/{petId}/appointments return 409 due to
	@Test
	public void shouldRejectDoubleBooking() throws Exception {

		int petId1 = 1;
		int petId2 = 2;
		AppointmentSlot slot = new AppointmentSlot();

		slot.setStartAt(LocalDateTime.of(2026, 10, 27, 15, 20));
		slot.setEndAt(LocalDateTime.of(2026, 10, 27, 15, 50));

		AppointmentSlot savedSlot = this.slots.saveAndFlush(slot);

		AppointmentRequest request = new AppointmentRequest(savedSlot.getId());

		mockMvc
			.perform(post("/api/pets/{petId}/appointments", petId1)
				.header("Idempotency-Key", UUID.randomUUID().toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated()); // first book

		mockMvc
			.perform(post("/api/pets/{petId}/appointments", petId2)
				.header("Idempotency-Key", UUID.randomUUID().toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isConflict()); // double book

	}

}
