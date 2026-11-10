package org.springframework.samples.petclinic.appointment;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("postgres")
public class AppointmentSlotRepositoryIntegrationTests {

	@Container
	@ServiceConnection

	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.3");

	@Autowired
	private AppointmentSlotRepository slots;

	@Autowired
	private AppointmentService aService;

	/*
	 *
	 * Arrange: 创建三个 Slot，其中一个已经被预约
	 *
	 * Act: 调用 findAvailableSlots(now)
	 *
	 * Assert: 已预约和过去的 Slot 被排除，未来未预约的 Slot 被返回
	 *
	 */
	@Test
	public void shouldReturnWhenFindingAvailableSlots() throws Exception {

		int petId = 8;

		AppointmentSlot slotA = new AppointmentSlot();
		slotA.setStartAt(LocalDateTime.of(2026, 11, 3, 18, 40));
		slotA.setEndAt(slotA.getStartAt().plusMinutes(30));

		AppointmentSlot slotB = new AppointmentSlot();
		slotB.setStartAt(LocalDateTime.of(2026, 11, 3, 19, 30));
		slotB.setEndAt(slotB.getStartAt().plusMinutes(30));

		AppointmentSlot slotC = new AppointmentSlot();
		slotC.setStartAt(LocalDateTime.of(2026, 11, 2, 10, 0));
		slotC.setEndAt(slotC.getStartAt().plusMinutes(30));

		List<AppointmentSlot> list = new ArrayList<>();
		list.add(slotA);
		list.add(slotB);
		list.add(slotC);

		this.slots.saveAllAndFlush(list);

		AppointmentRequest request = new AppointmentRequest(slotA.getId());

		this.aService.createAppointment(petId, request);

		LocalDateTime now = LocalDateTime.of(2026, 11, 3, 18, 0);

		List<AppointmentSlot> res = this.slots.findAvailableSlots(now);

		assertThat(res).extracting(AppointmentSlot::getId)
			.contains(slotB.getId())
			.doesNotContain(slotA.getId(), slotC.getId());

	}

}
