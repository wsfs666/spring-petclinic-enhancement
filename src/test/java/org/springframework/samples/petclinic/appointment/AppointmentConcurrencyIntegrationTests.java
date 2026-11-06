package org.springframework.samples.petclinic.appointment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.print.attribute.standard.Media;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("postgres")
public class AppointmentConcurrencyIntegrationTests {

	@Container
	@ServiceConnection

	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.3");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private AppointmentRepository appointments;

	@Autowired
	private AppointmentSlotRepository slots;

	@Autowired
	private OwnerRepository owners;

	@Autowired
	private ObjectMapper objectMapper;

	// 20 users compete for same slot

	@Test
	public void shouldAllowOneBookingUnderConcurrency() throws Exception {

		int petId = 1;

		AppointmentSlot slot = new AppointmentSlot();
		slot.setStartAt(LocalDateTime.of(2026, 10, 27, 12, 20));
		slot.setEndAt(slot.getStartAt().plusMinutes(30));

		AppointmentSlot savedSlot = this.slots.saveAndFlush(slot);

		AppointmentRequest request = new AppointmentRequest(savedSlot.getId());

		// mock concurrency and create by same slot

		int requestCount = 20;
		ExecutorService executor = Executors.newFixedThreadPool(requestCount);// 20
																				// concurrent
																				// users

		CountDownLatch readyLatch = new CountDownLatch(requestCount);
		CountDownLatch startLatch = new CountDownLatch(1);
		List<Future<Integer>> futures = new ArrayList<>();

		// why use try finally, avoid when 10s did not make counter = 0, then throw
		// exception and go to fianlly part to realse all the resources.
		try {

			for (int i = 0; i < requestCount; i++) {
				UUID key = UUID.randomUUID();
				Future<Integer> future = executor.submit(() -> {
					readyLatch.countDown();
					startLatch.await();
					return mockMvc
						.perform(post("/api/pets/{petId}/appointments", petId).header("Idempotency-Key", key.toString())
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
						.andReturn()
						.getResponse()
						.getStatus();
				});

				futures.add(future);

			}

			assertThat(readyLatch.await(10, TimeUnit.SECONDS)).isTrue();
			// if 10s not make counter == 0 then return false, and throw exception and
			// stop operating the rest of try part.

			startLatch.countDown();

			List<Integer> status = new ArrayList<>();

			for (Future<Integer> future : futures) {
				status.add(future.get(30, TimeUnit.SECONDS));
			}

			assertThat(status).filteredOn(s -> s == 201).hasSize(1);
			assertThat(status).filteredOn(s -> s == 409).hasSize(19);

		}
		finally {
			startLatch.countDown();
			executor.shutdown();
		}

		long count = this.appointments.countBySlot_IdAndStatus(savedSlot.getId(), AppointmentStatus.CONFIRMED);

		assertThat(count).isEqualTo(1L);

		// create 20 concurrent execution and do

		/*
		 * int requestCount = 20;
		 *
		 * ExecutorService executor = Executors.newFixedThreadPool(requestCount);
		 *
		 * CountDownLatch readyLatch = new CountDownLatch(requestCount); CountDownLatch
		 * startLatch = new CountDownLatch(1);
		 *
		 * List<Future<Integer>> futures = new ArrayList<>();
		 *
		 * try { for (int i = 0; i < requestCount; i++) { Future<Integer> future =
		 * executor.submit(() -> { readyLatch.countDown(); startLatch.await();
		 *
		 * return mockMvc .perform(post("/api/pets/{petId}/appointments",
		 * petId).contentType(MediaType.APPLICATION_JSON)
		 * .content(objectMapper.writeValueAsString(request))) .andReturn() .getResponse()
		 * .getStatus(); });
		 *
		 * futures.add(future); }
		 *
		 * assertThat(readyLatch.await(10, TimeUnit.SECONDS)).isTrue();
		 * startLatch.countDown();
		 *
		 * List<Integer> statuses = new ArrayList<>();
		 *
		 * for (Future<Integer> future : futures) { statuses.add(future.get(30,
		 * TimeUnit.SECONDS)); }
		 *
		 * assertThat(statuses).filteredOn(s -> s == 201).hasSize(1);
		 * assertThat(statuses).filteredOn(s -> s == 409).hasSize(19);
		 *
		 * } finally { startLatch.countDown(); executor.shutdownNow(); }
		 *
		 * // check cnt by status and slotId
		 *
		 * long count = appointments.countBySlot_IdAndStatus(savedSlot.getId(),
		 * AppointmentStatus.CONFIRMED);
		 *
		 * assertThat(count).isEqualTo(1L);
		 */
	}

}
