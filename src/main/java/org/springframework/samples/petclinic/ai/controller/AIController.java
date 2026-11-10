package org.springframework.samples.petclinic.ai.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.ai.agent.AgentReply;
import org.springframework.samples.petclinic.ai.agent.AgentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AIController {

	private final AgentService agentService;

	public AIController(AgentService agentService) {
		this.agentService = agentService;
	}

	@PostMapping("/api/ai/query")
	public String createInfoByAi(@RequestBody String message) {

		String result = this.agentService.ask(message);

		return result;

	}

	@PostMapping(value = "/api/ai/chat", consumes = MediaType.TEXT_PLAIN_VALUE)
	public AgentReply chat(@RequestBody String message) {
		return this.agentService.askWithBooking(message);
	}

}
