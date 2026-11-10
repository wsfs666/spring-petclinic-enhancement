package org.springframework.samples.petclinic.ai.controller;

import java.util.UUID;

import org.springframework.samples.petclinic.ai.agent.AgentBookingFacade;
import org.springframework.samples.petclinic.ai.agent.AgentService;
import org.springframework.samples.petclinic.appointment.AppointmentResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class ConfirmationController {

	private final AgentBookingFacade agentBookingFacade;

	public ConfirmationController(AgentBookingFacade agentBookingFacade) {
		this.agentBookingFacade = agentBookingFacade;
	}

	@ResponseBody
	@PostMapping("/api/ai/bookings/{confirmationId}/confirm")
	public AppointmentResponse confirmBookingApi(@PathVariable("confirmationId") UUID confirmationId) {
		return this.agentBookingFacade.confirmationBooking(confirmationId);

	}

}
