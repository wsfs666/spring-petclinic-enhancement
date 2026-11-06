package org.springframework.samples.petclinic.ai.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.samples.petclinic.ai.tool.GetAvailableSlotsTool;
import org.springframework.samples.petclinic.ai.tool.PetVisitHistoryTool;
import org.springframework.stereotype.Service;

@Service
public class AgentService {

	private final ChatClient chatClient;

	private final PetVisitHistoryTool petVisitHistoryTool;

	private final GetAvailableSlotsTool getAvailableSlotsTool;

	public AgentService(ChatClient.Builder chatClientBuilder, PetVisitHistoryTool petVisitHistoryTool,
			GetAvailableSlotsTool getAvailableSlotsTool) {
		this.chatClient = chatClientBuilder.build();
		this.petVisitHistoryTool = petVisitHistoryTool;
		this.getAvailableSlotsTool = getAvailableSlotsTool;
	}

	public String ask(String message) {

		return this.chatClient.prompt().system("You are a veterinary clinic assistant.\n" + //
				"Use getPetVisitHistoryTool to retrieve a pet's visit history only when a pet ID is provided.\n" + //
				"Use getAvailableSlotsTool to retrieve available future timeslots from the database.\n" + //
				"Do not invent appointment availability.\n" + //
				"Do not claim you can search pets, add visits, or schedule appointments.\n" + //
				"unless a corresponding tool is available.\n" + //
				"If a requested capability is unavailable, say that clearly.")
			.user(message)
			.tools(petVisitHistoryTool, getAvailableSlotsTool)
			.call()
			.content();

	}

}
