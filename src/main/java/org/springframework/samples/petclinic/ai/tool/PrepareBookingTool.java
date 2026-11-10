package org.springframework.samples.petclinic.ai.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.samples.petclinic.ai.agent.AgentBookingFacade;
import org.springframework.samples.petclinic.ai.agent.BookingPreview;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.ai.chat.model.ToolContext;

@Component
public class PrepareBookingTool implements AgentTool {

	private final AgentBookingFacade aBookingFacade;

	public PrepareBookingTool(AgentBookingFacade aBookingFacade) {
		this.aBookingFacade = aBookingFacade;
	}

	@Tool(description = "prepare a comfirmation booking for current petId and slotId. return booking detail for user to review and further confirmation. Do not make a appointment automatically.")
	public BookingPreview prepareBooking(@ToolParam(description = "unique id of pet") int petId,
			@ToolParam(description = "unique id of timeslot") int slotId, ToolContext toolContext) {
		BookingPreview bPreview = this.aBookingFacade.prepareBooking(petId, slotId);

		@SuppressWarnings("unchecked")
		AtomicReference<BookingPreview> holder = (AtomicReference<BookingPreview>) toolContext.getContext()
			.get("bookingHolder");

		if (holder != null) {
			holder.set(bPreview);
		}

		return bPreview;
	}

}
