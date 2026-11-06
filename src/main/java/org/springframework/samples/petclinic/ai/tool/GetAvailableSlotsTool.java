package org.springframework.samples.petclinic.ai.tool;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.samples.petclinic.appointment.AppointmentSlotService;
import org.springframework.samples.petclinic.appointment.AvailableSlotResponse;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component

public class GetAvailableSlotsTool {

	private AppointmentSlotService appointmentSlotService;

	private static final Logger log = LoggerFactory.getLogger(GetAvailableSlotsTool.class);

	public GetAvailableSlotsTool(AppointmentSlotService appointmentSlotService) {
		this.appointmentSlotService = appointmentSlotService;
	}

	@Tool(description = "get current available future appointment slots from the clinic database ")
	public List<AvailableSlotResponse> getAvailableSlots() {
		log.info("getAvailableSlots tool invoked");
		return this.appointmentSlotService.getAvailableSlots();

	}

}
