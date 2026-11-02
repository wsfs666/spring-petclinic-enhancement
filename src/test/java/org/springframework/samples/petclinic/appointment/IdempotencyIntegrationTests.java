package org.springframework.samples.petclinic.appointment;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeEditor;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("postgres")
public class IdempotencyIntegrationTests {

	@Container
	@ServiceConnection

	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.3");

	@Autowired
	private IdempotencyReceiptRepository receipts;

	@Autowired
	private AppointmentSlotRepository slots;

	@Autowired
	private OwnerRepository owners;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private AppointmentRepository appointments;

	@MockitoSpyBean
	private AppointmentService aService;

	/*
	 * test1 POST /api/pets/1/appointments Idempotency-Key:
	 * 550e8400-e29b-41d4-a716-446655440000 Content-Type: application/json {"slotId": 7}
	 *
	 * 预期：创建一个 Appointment，同时新增一条 Receipt，appointment_id 被正确填入。
	 *
	 *
	 */

	@Test
	public void shouldReturnWhenCreateAppointment() throws Exception {

		// mock ARequest, setIdempotency-Key, slotId = 7, petId = 1

		AppointmentSlot slot = new AppointmentSlot();
		slot.setStartAt(LocalDateTime.of(2026, 10, 28, 16, 48));
		slot.setEndAt(slot.getStartAt().plusMinutes(30));

		AppointmentSlot savedSlot = this.slots.saveAndFlush(slot);
		AppointmentRequest request = new AppointmentRequest(savedSlot.getId());

		UUID key = UUID.randomUUID();
		int petId = 1;
		String operation = "CREATE_APPOINTMENT";

		// action post to create appointment

		mockMvc
			.perform(post("/api/pets/{petId}/appointments", petId).header("Idempotency-Key", key.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

		IdempotencyReceipt receipt = this.receipts
			.findByPrincipalIdAndOperationAndIdempotencyKey(this.owners.findOwnerByPetId(petId).orElseThrow().getId(),
					operation, key)
			.orElseThrow();

		assertThat(receipt.getAppointmentId()).isNotNull();

	}

	/*
	 * test2 测试相同 Key、相同 Payload 同一个测试中连续发送两次 这里预期第二次仍是 201，因为你当前 Controller 固定返回 CREATED。
	 *
	 * The duplicate-request test also covers the server-side recovery behavior for a
	 * lost-response scenario. Once the first transaction commits, retrying with the same
	 * idempotency key returns the existing appointment without creating a duplicate.
	 *
	 *
	 *
	 */

	@Test
	public void shouldReturnExistingAppointmentWhenSameKeyRetried() throws Exception {

		// mock
		int petId = 2;
		AppointmentSlot slot = new AppointmentSlot();
		slot.setStartAt(LocalDateTime.now());
		slot.setEndAt(slot.getStartAt().plusMinutes(30));
		AppointmentSlot savedSlot = this.slots.saveAndFlush(slot);

		UUID key = UUID.randomUUID();
		AppointmentRequest request = new AppointmentRequest(savedSlot.getId());

		// action
		MvcResult first = mockMvc
			.perform(post("/api/pets/{petId}/appointments", petId).header("Idempotency-Key", key.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andReturn();

		MvcResult second = mockMvc
			.perform(post("/api/pets/{petId}/appointments", petId).header("Idempotency-Key", key.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andReturn();

		AppointmentResponse first_Response = objectMapper.readValue(first.getResponse().getContentAsString(),
				AppointmentResponse.class);
		AppointmentResponse second_Response = objectMapper.readValue(second.getResponse().getContentAsString(),
				AppointmentResponse.class);

		assertThat(first_Response.id()).isEqualTo(second_Response.id());

	}

	/*
	 * test3 相同 Idempotency-Key，不同 Request → 409
	 *
	 *
	 *
	 *
	 *
	 */
	@Test
	public void shouldReturnWhenDifferentRequest() throws Exception {

		// mock
		int petId = 3;
		AppointmentSlot slot1 = new AppointmentSlot();
		slot1.setStartAt(LocalDateTime.of(2026, 10, 28, 17, 10));
		slot1.setEndAt(slot1.getStartAt().plusMinutes(30));

		AppointmentSlot slot2 = new AppointmentSlot();
		slot2.setStartAt(LocalDateTime.of(2026, 10, 28, 19, 10));
		slot2.setEndAt(slot2.getStartAt().plusMinutes(30));

		AppointmentSlot savedSlot1 = this.slots.saveAndFlush(slot1);
		AppointmentSlot savedSlot2 = this.slots.saveAndFlush(slot2);

		AppointmentRequest r1 = new AppointmentRequest(savedSlot1.getId());
		AppointmentRequest r2 = new AppointmentRequest(savedSlot2.getId());

		UUID key = UUID.randomUUID();

		// action
		mockMvc
			.perform(post("/api/pets/{petId}/appointments", petId).header("Idempotency-Key", key.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(r1)))
			.andExpect(status().isCreated());

		mockMvc
			.perform(post("/api/pets/{petId}/appointments", petId).header("Idempotency-Key", key.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(r2)))
			.andExpect(status().isConflict());

	}

	/*
	 * test4 测试场景：不同 Key，竞争同一个 Slot 1st return 201 2nd receipt should rollback how to
	 * test?
	 *
	 *
	 */

	@Test
	public void shouldReturnWhenCompeteSameSlot() throws Exception {

		// mock
		int petId1 = 4;
		int petId2 = 3;

		Owner owner1 = this.owners.findOwnerByPetId(petId1).orElseThrow();
		Owner owner2 = this.owners.findOwnerByPetId(petId2).orElseThrow();

		UUID key1 = UUID.randomUUID();
		UUID key2 = UUID.randomUUID();

		AppointmentSlot slot = new AppointmentSlot();
		slot.setStartAt(LocalDateTime.of(2026, 10, 28, 18, 2));
		slot.setEndAt(slot.getStartAt().plusMinutes(30));

		AppointmentSlot savedSlot = this.slots.saveAndFlush(slot);

		AppointmentRequest r = new AppointmentRequest(savedSlot.getId());

		String operation = "CREATE_APPOINTMENT";

		// action
		mockMvc
			.perform(post("/api/pets/{petId}/appointments", petId1).header("Idempotency-Key", key1.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(r)))
			.andExpect(status().isCreated());

		mockMvc
			.perform(post("/api/pets/{petId}/appointments", petId2).header("Idempotency-Key", key2.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(r)))
			.andExpect(status().isConflict());

		assertThat(this.receipts.findByPrincipalIdAndOperationAndIdempotencyKey(owner1.getId(), operation, key1))
			.isNotEmpty();
		assertThat(this.receipts.findByPrincipalIdAndOperationAndIdempotencyKey(owner2.getId(), operation, key2))
			.isEmpty();

	}

	/*
	 * test5 20 个并发请求使用相同的 Idempotency-Key 和相同 Payload。
	 *
	 * 这个测试不再主要验证 Slot 唯一约束，而是验证 uq_key_comb 和 ON CONFLICT DO NOTHING 能不能让所有重复请求最终返回同一个
	 * Appointment ID，且数据库只创建一条预约。
	 *
	 *
	 *
	 */
	private byte[] calculateRequestHash(int petId, AppointmentRequest request) {
		String content = petId + ":" + request.slotId();

		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(content.getBytes(StandardCharsets.UTF_8));
			return md.digest();
		}
		catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 unavailable", e);
		}
	}

	@Test
	public void shouldReturnWhenConcurrentRequest() throws Exception {

		int petId = 5;
		Owner owner = this.owners.findOwnerByPetId(petId).orElseThrow();
		UUID key = UUID.randomUUID();
		AppointmentSlot slot = new AppointmentSlot();
		slot.setStartAt(LocalDateTime.of(2026, 10, 29, 10, 8));
		slot.setEndAt(slot.getStartAt().plusMinutes(30));
		AppointmentSlot savedSlot = this.slots.saveAndFlush(slot);
		AppointmentRequest request = new AppointmentRequest(savedSlot.getId());
		String operation = "CREATE_APPOINTMENT";

		int requestCount = 20;
		ExecutorService executor = Executors.newFixedThreadPool(requestCount);
		CountDownLatch readyLatch = new CountDownLatch(requestCount);
		CountDownLatch startLatch = new CountDownLatch(1);
		List<Future<MvcResult>> futures = new ArrayList<>();

		try {
			for (int i = 0; i < requestCount; i++) {
				Future<MvcResult> future = executor.submit(() -> {
					readyLatch.countDown();
					startLatch.await();
					return mockMvc
						.perform(post("/api/pets/{petId}/appointments", petId).header("Idempotency-Key", key.toString())
							.contentType(MediaType.APPLICATION_JSON)
							.content(objectMapper.writeValueAsString(request)))
						.andExpect(status().isCreated())
						.andReturn();

				});

				futures.add(future);

			}

			assertThat(readyLatch.await(30, TimeUnit.SECONDS)).isTrue();

			startLatch.countDown();

			List<AppointmentResponse> responses = new ArrayList<>();
			for (Future<MvcResult> f : futures) {
				AppointmentResponse response = objectMapper.readValue(
						f.get(30, TimeUnit.SECONDS).getResponse().getContentAsString(), AppointmentResponse.class);
				responses.add(response);
			}

			// created receipt

			// if all appointmentId return same
			int appointId = responses.get(0).id();
			assertThat(responses).filteredOn(r -> r.id() == appointId).hasSize(20);

			// how many records created in receipts
			assertThat(this.receipts.countByPrincipalIdAndOperationAndIdempotencyKey(owner.getId(), operation, key))
				.isEqualTo(1L);

			// is the record has the same app_id as response
			IdempotencyReceipt receipt = receipts
				.findByPrincipalIdAndOperationAndIdempotencyKey(owner.getId(), operation, key)
				.orElseThrow();

			assertThat(receipt.getAppointmentId()).isEqualTo(appointId);

		}
		finally {

			startLatch.countDown();
			executor.shutdown();

		}

	}

	/*
	 * test6
	 *
	 * Failure Before Commit: inject failture after writing new receipt and new
	 * appointment in table and before transaction committing
	 *
	 * POST /api/pets/1/appointments
	 *
	 *
	 *
	 *
	 *
	 */

	@Test
	public void shouldReturnWhenFailureInjection() throws Exception {

		int petId = 2;
		UUID key = UUID.randomUUID();
		AppointmentSlot slot = new AppointmentSlot();
		slot.setStartAt(LocalDateTime.of(2026, 11, 1, 11, 35));
		slot.setEndAt(slot.getStartAt().plusMinutes(30));
		AppointmentSlot savedSlot = this.slots.saveAndFlush(slot);

		AppointmentRequest request = new AppointmentRequest(savedSlot.getId());
		Owner owner = this.owners.findOwnerByPetId(petId).orElseThrow();
		String operation = "CREATE_APPOINTMENT";

		/**
		 * doAnswer when: 我关心它如何执行，以及在执行到哪里时失败。 when ReturnThrow: 一执行方法就抛出异常，但是我们需要执行完再抛出
		 *
		 *
		 *
		 *
		 */
		doAnswer(invocation -> {
			invocation.callRealMethod();
			throw new RuntimeException("inject failure");
		}).when(this.aService).createAppointment(eq(petId), any(AppointmentRequest.class));

		assertThatThrownBy(() -> mockMvc
			.perform(post("/api/pets/{petId}/appointments", petId).header("Idempotency-Key", key.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))))
			.hasStackTraceContaining("failure");

		long appoint_cnt = this.appointments.countBySlot_IdAndStatus(savedSlot.getId(), AppointmentStatus.CONFIRMED);

		assertThat(appoint_cnt).isZero();

		long receipt_cnt = this.receipts.countByPrincipalIdAndOperationAndIdempotencyKey(owner.getId(), operation, key);

		assertThat(receipt_cnt).isZero();

		/*
		 * test7 failure recovery: cancel failure rejection->same Key K1 + same Pet + same
		 * Slot → 201
		 */

		// cancel rejection
		doCallRealMethod().when(this.aService).createAppointment(eq(petId), any(AppointmentRequest.class));

		mockMvc
			.perform(post("/api/pets/{petId}/appointments", petId).header("Idempotency-Key", key.toString())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated());

	}

}
