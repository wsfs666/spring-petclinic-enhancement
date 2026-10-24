package org.springframework.samples.petclinic.ai.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.samples.petclinic.ai.tool.PetVisitHistoryTool;
import org.springframework.stereotype.Service;

@Service
public class AgentService {

	private final ChatClient chatClient;

	private final PetVisitHistoryTool petVisitHistoryTool;

	public AgentService(ChatClient.Builder chatClientBuilder, PetVisitHistoryTool petVisitHistoryTool) {
		this.chatClient = chatClientBuilder.build();
		this.petVisitHistoryTool = petVisitHistoryTool;
	}

	public String ask(String message) {

		return this.chatClient.prompt().system("You can retrieve pet visit history only when a pet ID is provided.\n" + //
				"Do not claim you can search pets, add visits, or schedule appointments\n" + //
				"unless a corresponding tool is available.\n" + //
				"If a requested capability is unavailable, say that clearly.")
			.user(message)
			.tools(petVisitHistoryTool)
			.call()
			.content();

	}

}
