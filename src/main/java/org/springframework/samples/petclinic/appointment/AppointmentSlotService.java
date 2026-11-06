package org.springframework.samples.petclinic.appointment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentSlotService {

	private AppointmentSlotRepository slots;

	public AppointmentSlotService(AppointmentSlotRepository slots) {
		this.slots = slots;
	}

	@Transactional(readOnly = true)
	public List<AvailableSlotResponse> getAvailableSlots() {

		List<AppointmentSlot> list = this.slots.findAvailableSlots(LocalDateTime.now());

		return list.stream()
			.map(slot -> new AvailableSlotResponse(slot.getId(), slot.getStartAt(), slot.getEndAt()))
			.toList();

	}

}
