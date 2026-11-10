package org.springframework.samples.petclinic.ai.agent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.samples.petclinic.ai.tool.AgentTool;
import org.springframework.samples.petclinic.ai.tool.PetVisitHistoryTool;
import org.springframework.stereotype.Service;

@Service
public class AgentService {

	private final ChatClient chatClient;

	private final List<AgentTool> tools;

	// private final PetVisitHistoryTool petVisitHistoryTool;

	// private final GetAvailableSlotsTool getAvailableSlotsTool;

	public AgentService(ChatClient.Builder chatClientBuilder, List<AgentTool> tools) {
		this.chatClient = chatClientBuilder.build();
		// this.petVisitHistoryTool = petVisitHistoryTool;
		// this.getAvailableSlotsTool = getAvailableSlotsTool;
		this.tools = tools;
	}

	public String ask(String message) {

		return this.chatClient.prompt().system("You are a veterinary clinic assistant.\n" + //
				"Use GetPetVisitHistoryTool to retrieve a pet's visit history only when a pet ID is provided.\n" + //
				"Use GetAvailableSlotsTool to retrieve available future timeslots from the database.\n" + //
				"Do not invent appointment availability.\n" + //
				"Do not claim you can search pets, add visits, or schedule appointments.\n" + //
				"unless a corresponding tool is available.\n" + //
				"If a requested capability is unavailable, say that clearly.")
			.user(message)
			.tools(tools)
			.call()
			.content();

	}

	public AgentReply askWithBooking(String message) {
		// convey toolcontext and return agentReply template
		AtomicReference<BookingPreview> holder = new AtomicReference<>();
		String response = this.chatClient.prompt().system("You are a veterinary clinic assistant.\n" + //
				"Use getPetVisitHistoryTool to retrieve a pet's visit history only when a pet ID is provided.\n" + //
				"Use getAvailableSlotsTool to retrieve available future timeslots from the database.\n" + //
				"Use prepareBookingTool to prepare a comfirmation booking for current petId and slotId.\n" + //
				"A prepared booking is NOT a confirmed appointment.\n" + //
				"Ask the user to review and confirm the booking.\n" + //
				"Never claim an appointment was created before the backend confirms it.\n" + //
				"Do not invent appointment availability or booking results.\n" + //
				"When answering questions using clinic policies, cite the sourceId of the supporting policy documents.\n"
				+ //
				"Do not invent clinic policies or unsupported actions.\n" + //
				"Answer each part of the user's question explicitly.\n" + //
				"Distinguish between whether an action is allowed and whether a fee applies.\n" + //
				"Cite only policies provided by retrieved knowledge.\n" + //
				"Group citations at the end of the answer.\n" + //
				"Avoid repeating the same policy citation unnecessarily.")
			.user(message)
			.toolContext(Map.of("bookingHolder", holder))
			.tools(tools)
			.call()
			.content();

		AgentReply aReply = new AgentReply(response, holder.get());
		return aReply;

	}

}
